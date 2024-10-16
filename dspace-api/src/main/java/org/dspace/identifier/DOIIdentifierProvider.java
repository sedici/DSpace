/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.identifier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.FormatIdentifier;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.core.Context;
import org.dspace.identifier.doi.DOIConnector;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.identifier.doi.service.DOIFilterService;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provide service for DOIs using DataCite.
 * 
 * <p>This class handles reservation, registration and deletion of DOIs using
 * the direct API from {@link <a href="http://www.datacite.org">DataCite</a>}.
 * Please pay attention that some members of DataCite offer special services
 * and want their customers to use special APIs. If you are unsure ask your
 * registration agency.</p>
 * 
 * <p>Any identifier a method of this class returns is a string in the following format: doi:10.123/456.</p>
 * 
 * @author Pascal-Nicolas Becker
 */
public class DOIIdentifierProvider
    extends IdentifierProvider
{
    private static final Logger log = LoggerFactory.getLogger(DOIIdentifierProvider.class);

    /**
     * A DOIConnector connects the DOIIdentifierProvider to the API of the DOI
     * registration agency needed to register DOIs. To register DOIs we have to
     * care about two APIs: the <link>IdentifierProvider</link> API of DSpace
     * and the API of the DOI registration agency. The DOIIdentifierProvider
     * manages the DOI database table, generates new DOIs, stores them as
     * metadata in DSpace items and so on. To register DOIs at DOI registration
     * agencies it uses a DOIConnector. A DOI connector has to register and
     * reserve DOIs using the API of the DOI registration agency. If requested
     * by the registration agency it has to convert and send metadata of the
     * DSpace items.
     */
    private DOIConnector connector;
    
    protected DOIFilterService doiFilterService;

    static final String CFG_PREFIX = "identifier.doi.prefix";
    static final String CFG_NAMESPACE_SEPARATOR = "identifier.doi.namespaceseparator";
        
    // Metadata field name elements
    // TODO: move these to MetadataSchema or some such?
    public static final String MD_SCHEMA = "dc";
    public static final String DOI_ELEMENT = "identifier";
    public static final String DOI_QUALIFIER = "uri";
    
    public static final Integer TO_BE_REGISTERED = 1;
    public static final Integer TO_BE_RESERVED = 2;
    public static final Integer IS_REGISTERED = 3;
    public static final Integer IS_RESERVED = 4;
    public static final Integer UPDATE_RESERVED = 5;
    public static final Integer UPDATE_REGISTERED = 6;
    public static final Integer UPDATE_BEFORE_REGISTRATION = 7;
    public static final Integer TO_BE_DELETED = 8;
    public static final Integer DELETED = 9;
    
    /**
     * Prefix of DOI namespace. Set in dspace.cfg.
     */
    private String PREFIX;
    
    /**
     * Part of DOI to separate several applications that generate DOIs.
     * E.g. it could be 'dspace/' if DOIs generated by DSpace should have the form
     * prefix/dspace/uniqueString. Set it to the empty String if DSpace must
     * generate DOIs directly after the DOI Prefix. Set in dspace.cfg.
     */
    private String NAMESPACE_SEPARATOR;
    
    protected String getPrefix()
    {
        if (null == this.PREFIX)
        {
            this.PREFIX = this.configurationService.getProperty(CFG_PREFIX);
            if (null == this.PREFIX)
            {
                log.warn("Cannot find DOI prefix in configuration!");
                throw new RuntimeException("Unable to load DOI prefix from "
                        + "configuration. Cannot find property " +
                        CFG_PREFIX + ".");
            }
        }
        return this.PREFIX;
    }
    
    protected String getNamespaceSeparator()
    {
        if (null == this.NAMESPACE_SEPARATOR)
        {
            this.NAMESPACE_SEPARATOR = this.configurationService.getProperty(CFG_NAMESPACE_SEPARATOR);
            if (null == this.NAMESPACE_SEPARATOR)
            {
                this.NAMESPACE_SEPARATOR = "";
            }
        }
        return this.NAMESPACE_SEPARATOR;
    }

    @Required
    public void setDOIConnector(DOIConnector connector)
    {
        this.connector = connector;
    }
    
    @Required
    public void setdoiFilterService(DOIFilterService doiFilterService) {
        this.doiFilterService = doiFilterService;
    }

    /**
     * This identifier provider supports identifiers of type
     * {@link org.dspace.identifier.DOI}.
     * @param identifier to check if it will be supported by this provider.
     * @return 
     */
    @Override
    public boolean supports(Class<? extends Identifier> identifier)
    {
        return DOI.class.isAssignableFrom(identifier);
    }
    
    /**
     * This identifier provider supports identifiers in the following format:
     * <ul>
     *  <li>doi:10.123/456</li>
     *  <li>10.123/456</li>
     *  <li>http://dx.doi.org/10.123/456</li>
     * </ul>
     * @param identifier to check if it is in a supported format.
     * @return 
     */
    @Override
    public boolean supports(String identifier)
    {
        try {
            DOI.formatIdentifier(identifier);
        } catch (IdentifierException e) {
            return false;
        } catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }

    
    @Override
    public String register(Context context, DSpaceObject dso)
            throws IdentifierException
    {
        // Solo registrar el item si cumple ciertas condiciones
        if (!doiFilterService.isEligibleDSO(dso)) {
            log.info("Couldn't register doi for DSO with handle " + dso.getHandle()
                    + ", the DSO does not meet the conditions that the repository imposes to have doi");
            return null;
        }
        if (doiFilterService.hasExternalDOI(dso)) {
            log.info("Couldn't register doi for DSO with handle " + dso.getHandle()
                + ", the DSO already has an external DOI.");
            return null;
        }
        String doi = mint(context, dso);
        // register tries to reserve doi if it's not already.
        // So we don't have to reserve it here.
        this.register(context, dso, doi);
        return doi;
    }


    @Override
    public void register(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException
    {
        // Solo registrar el item si cumple ciertas condiciones
        if (!doiFilterService.isEligibleDSO(dso)) {
            log.info("Couldn't register doi for DSO with handle " + dso.getHandle()
                    + ", the DSO does not meet the conditions that the repository imposes to have doi");
            return;
        }
        if (doiFilterService.hasExternalDOI(dso)) {
            log.info("Couldn't register doi for DSO with handle " + dso.getHandle()
                + ", the DSO already has an external DOI.");
            return;
        }
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = null;

        // search DOI in our db
        try
        {
            doiRow = loadOrCreateDOI(context, dso, doi);
        } catch (SQLException ex) {
            log.error("Error in databse connection: " + ex.getMessage());
            throw new RuntimeException("Error in database conncetion.", ex);
        }

        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to register a DOI that "
                    + "is marked as DELETED.", DOIIdentifierException.DOI_IS_DELETED);
        }

        // Check status of DOI
        if (IS_REGISTERED == doiRow.getIntColumn("status"))
        {
            return;
        }
        
        // change status of DOI
        doiRow.setColumn("status", TO_BE_REGISTERED);
        try {
            DatabaseManager.update(context, doiRow);
            context.commit();
        }
        catch (SQLException sqle)
        {
            log.warn("SQLException while changing status of DOI {} to be registered.", doi);
            throw new RuntimeException(sqle);
        }
    }

    /**
     * @param context
     * @param dso DSpaceObject the DOI should be reserved for. Some metadata of
     *            this object will be send to the registration agency.
     * @param identifier DOI to register in a format that
     *                   {@link FormatIdentifier(String)} accepts.
     * @throws IdentifierException If the format of {@code identifier} was
     *                             unrecognized or if it was impossible to 
     *                             reserve the DOI (registration agency denied 
     *                             for some reason, see logs).
     * @throws IllegalArgumentException If {@code identifier} is a DOI already
     *                                  registered for another DSpaceObject then
     *                                  {@code dso}.
     * @see IdentifierProvider.reserve(Context, DSpaceObject, String)
     */
    @Override
    public void reserve(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException, IllegalArgumentException
    {
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = null;
        
        try {
            // if the doi is in our db already loadOrCreateDOI just returns.
            // if it is not loadOrCreateDOI safes the doi.
            doiRow = loadOrCreateDOI(context, dso, doi);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        if (!doiRow.isColumnNull("status")) {
            return;
        } 
                
        doiRow.setColumn("status", TO_BE_RESERVED);
        try
        {
            DatabaseManager.update(context, doiRow);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }

    public void reserveOnline(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException, IllegalArgumentException, SQLException
    {        
        String doi = DOI.formatIdentifier(identifier);
        // get TableRow and ensure DOI belongs to dso regarding our db
        TableRow doiRow = loadOrCreateDOI(context, dso, doi);
        
        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to reserve a DOI that "
                    + "is marked as DELETED.", DOIIdentifierException.DOI_IS_DELETED);
        }
        
        connector.reserveDOI(context, dso, doi);
        
        doiRow.setColumn("status", IS_RESERVED);
        DatabaseManager.update(context, doiRow);
    }

    public void registerOnline(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException, IllegalArgumentException, SQLException
    {
        if (doiFilterService.hasExternalDOI(dso)) {
            throw new DOIIdentifierException("Cannot register for new DOI when item "
                    + "already has an external DOI.", DOIIdentifierException.FOREIGN_DOI);
        }
        String doi = DOI.formatIdentifier(identifier);
        // get TableRow and ensure DOI belongs to dso regarding our db
        TableRow doiRow = loadOrCreateDOI(context, dso, doi);
        
        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to register a DOI that "
                    + "is marked as DELETED.", DOIIdentifierException.DOI_IS_DELETED);
        }
        
        // In case register fails, status must not stay null
        if (doiRow.isColumnNull("status")) {
            doiRow.setColumn("status", TO_BE_REGISTERED);
            if (0 == DatabaseManager.update(context, doiRow))
            {
                throw new RuntimeException("Cannot update DOI status in the database for unkown reason.");
            }
        }

        // register DOI Online
        try {
            connector.registerDOI(context, dso, doi);
        }
        catch (DOIIdentifierException die)
        {
            // do we have to reserve DOI before we can register it?
            if (die.getCode() == DOIIdentifierException.RESERVE_FIRST)
            {
                this.reserveOnline(context, dso, identifier);
                connector.registerDOI(context, dso, doi);
            }
            else
            {
                throw die;
            }
        }

        // safe DOI as metadata of the item
        try {
            saveDOIToObject(context, dso, doi);
        }
        catch (AuthorizeException ae)
        {
            throw new IdentifierException("Not authorized to save a DOI as metadata of an dso!", ae);
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
        
        doiRow.setColumn("status", IS_REGISTERED);
        DatabaseManager.update(context, doiRow);
        dso.resetIdentifiersCache();
    }
    
    public void updateMetadata(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException, IllegalArgumentException, SQLException 
    {
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = null;
        
        doiRow = loadOrCreateDOI(context, dso, doi);

        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to register a DOI that "
                    + "is marked as DELETED.", DOIIdentifierException.DOI_IS_DELETED);
        }

        if (IS_REGISTERED == doiRow.getIntColumn("status")) 
        {
            doiRow.setColumn("status", UPDATE_REGISTERED);
        }
        else if (TO_BE_REGISTERED == doiRow.getIntColumn("status")) 
        {
            doiRow.setColumn("status", UPDATE_BEFORE_REGISTRATION);
        }
        else if (IS_RESERVED == doiRow.getIntColumn("status")) 
        {
            doiRow.setColumn("status", UPDATE_RESERVED);
        }
        else
        {
            return;
        }

        DatabaseManager.update(context, doiRow);
    }
    
    public void updateMetadataOnline(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException, SQLException
    {
        String doi = DOI.formatIdentifier(identifier);

        // ensure DOI belongs to dso regarding our db
        TableRow doiRow = null;
        try
        {
            doiRow = DatabaseManager.findByUnique(context, "Doi", "doi", doi.substring(DOI.SCHEME.length()));
        }
        catch (SQLException sqle)
        {
            log.warn("SQLException while searching a DOI in our db.", sqle);
            throw new RuntimeException("Unable to retrieve information about "+
                    "a DOI out of database.", sqle);
        }
        if (null == doiRow)
        {
            log.error("Cannot update metadata for DOI {}: unable to find it in "
                    + "our db.", doi);
            throw new DOIIdentifierException("Unable to find DOI.",
                    DOIIdentifierException.DOI_DOES_NOT_EXIST);
        }
        if (doiRow.getIntColumn("resource_id") != dso.getID() ||
                doiRow.getIntColumn("resource_type_id") != dso.getType())
        {
            log.error("Refuse to update metadata of DOI {} with the metadata of "
                    + " an object ({}/{}) the DOI is not dedicated to.",
                    new String[] {doi, dso.getTypeText(), Integer.toString(dso.getID())});
            throw new DOIIdentifierException("Cannot update DOI metadata: "
                    + "DOI and DSpaceObject does not match!",
                    DOIIdentifierException.MISMATCH);
        }

        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to update the metadata"
                    + "of a DOI that is marked as DELETED.",
                    DOIIdentifierException.DOI_IS_DELETED);
        }
        
        connector.updateMetadata(context, dso, doi);
        
        if (UPDATE_REGISTERED == doiRow.getIntColumn("status")) 
        {
            doiRow.setColumn("status", IS_REGISTERED);
        }
        else if (UPDATE_BEFORE_REGISTRATION == doiRow.getIntColumn("status"))
        {
            doiRow.setColumn("status", TO_BE_REGISTERED);
        }
        else if (UPDATE_RESERVED == doiRow.getIntColumn("status"))
        {
            doiRow.setColumn("status", IS_RESERVED);
        }
        
        DatabaseManager.update(context, doiRow);
    }
    
    @Override
    public String mint(Context context, DSpaceObject dso)
            throws IdentifierException
    {
        String doi = null;
        try
        {
            doi = getDOIByObject(context, dso);
        }
        catch (SQLException e)
        {
            log.error("Error while attemping to retrieve information about a DOI for "
                    + dso.getTypeText() + " with ID " + dso.getID() + ".");
            throw new RuntimeException("Error while attempting to retrieve " +
                    "information about a DOI for " + dso.getTypeText() + 
                    " with ID " + dso.getID() + ".", e);
        }
        if (null == doi)
        {
            try
            {
                TableRow doiRow = loadOrCreateDOI(context, dso, null);
                doi = DOI.SCHEME + doiRow.getStringColumn("doi");
                
            }
            catch (SQLException e)
            {
                log.error("Error while creating new DOI for Object of " +
                        "ResourceType {} with id {}.", dso.getType(), dso.getID());
                throw new RuntimeException("Error while attempting to create a " +
                        "new DOI for " + dso.getTypeText() + " with ID " + 
                        dso.getID() + ".", e);
            }
        }
        return doi;
    }

    @Override
    public DSpaceObject resolve(Context context, String identifier, String... attributes)
            throws IdentifierNotFoundException, IdentifierNotResolvableException
    {
        String doi = null;
        try {
            doi = DOI.formatIdentifier(identifier);
        } catch (IdentifierException e) {
            throw new IdentifierNotResolvableException(e);
        }
        try
        {
            DSpaceObject dso = getObjectByDOI(context, doi);
            if (null == dso)
            {
                throw new IdentifierNotFoundException();
            }
            return dso;
        }
        catch (SQLException sqle)
        {
            log.error("SQLException while searching a DOI in our db.", sqle);
            throw new RuntimeException("Unable to retrieve information about "+
                    "a DOI out of database.", sqle);
        }
        catch (IdentifierException e)
        {
            throw new IdentifierNotResolvableException(e);
        }
    }

    @Override
    public String lookup(Context context, DSpaceObject dso)
            throws IdentifierNotFoundException, IdentifierNotResolvableException
    {
        String doi = null;
        try
        {
            doi = getDOIByObject(context, dso);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving DOI out of database.", e);
        }
        
        if (null == doi)
        {
            throw new IdentifierNotFoundException("No DOI for DSpaceObject of type "
                    + dso.getTypeText() + " with ID " + dso.getID() + " found.");
        }
        
        return doi;
    }

    @Override
    public void delete(Context context, DSpaceObject dso)
            throws IdentifierException
    {
        // delete all DOIs for this Item from our database.
        try
        {
            String doi = getDOIByObject(context, dso);
            while (null != doi)
            {
                this.delete(context, dso, doi);
                doi = getDOIByObject(context, dso);
            }
        }
        catch (SQLException ex)
        {
            log.error("Error while attemping to retrieve information about a DOI for "
                    + dso.getTypeText() + " with ID " + dso.getID() + ".", ex);
            throw new RuntimeException("Error while attempting to retrieve " +
                    "information about a DOI for " + dso.getTypeText() + 
                    " with ID " + dso.getID() + ".", ex);
        }
        
        // delete all DOIs of this item out of its metadata
        try {
            String doi = getDOIOutOfObject(dso);
        
            while (null != doi)
            {
                this.removeDOIFromObject(context, dso, doi);
                doi = getDOIOutOfObject(dso);
            }
        }
        catch (AuthorizeException ex)
        {
            log.error("Error while removing a DOI out of the metadata of an "
                    + dso.getTypeText() + " with ID " + dso.getID() + ".", ex);
            throw new RuntimeException("Error while removing a DOI out of the "
                    + "metadata of an " + dso.getTypeText() + " with ID "
                    + dso.getID() + ".", ex);

        }
        catch (SQLException ex)
        {
            log.error("Error while removing a DOI out of the metadata of an "
                    + dso.getTypeText() + " with ID " + dso.getID() + ".", ex);
            throw new RuntimeException("Error while removing a DOI out of the "
                    + "metadata of an " + dso.getTypeText() + " with ID "
                    + dso.getID() + ".", ex);
        }
    }

    @Override
    public void delete(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException
    {
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = null;
        
        try
        {
            doiRow = DatabaseManager.findByUnique(context, "Doi", "doi",
                                            doi.substring(DOI.SCHEME.length()));
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }

        // check if DOI belongs to dso
        if (null != doiRow)
        {
            if (doiRow.getIntColumn("resource_id") != dso.getID() ||
                    doiRow.getIntColumn("resource_type_id") != dso.getType())
            {
                throw new DOIIdentifierException("Trying to delete a DOI out of "
                        + "an object that is not addressed by the DOI.",
                        DOIIdentifierException.MISMATCH);
            }
        }
        
        // remove DOI from metadata
        try
        {
            removeDOIFromObject(context, dso, doi);
        }
        catch (AuthorizeException ex)
        {
            log.error("Not authorized to delete a DOI out of an Item.", ex);
            throw new DOIIdentifierException("Not authorized to delete DOI.",
                    ex, DOIIdentifierException.UNAUTHORIZED_METADATA_MANIPULATION);
        }
        catch (SQLException ex)
        {
            log.error("SQLException occurred while deleting a DOI out of an item: "
                    + ex.getMessage());
            throw new RuntimeException("Error while deleting a DOI out of the " +
                    "metadata of an Item " + dso.getID(), ex);
        }
        
        // change doi status in db if necessary.
        if (null != doiRow)
        {
            if(doiRow.isColumnNull("status"))
            {
            doiRow.setColumn("status", DELETED);
            }
            else
            {
            doiRow.setColumn("status", TO_BE_DELETED);
            }
            try {
                DatabaseManager.update(context, doiRow);
                context.commit();
            }
            catch (SQLException sqle)
            {
                log.warn("SQLException while changing status of DOI {} to be deleted.", doi);
                throw new RuntimeException(sqle);
            }
         }

        // DOI is a permanent identifier. DataCite for example does not delete
        // DOIS. But it is possible to mark a DOI as "inactive".
    }
    
    public void deleteOnline(Context context, String identifier) 
            throws DOIIdentifierException
    {
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = null;
        
        try 
        {
            doiRow = DatabaseManager.findByUnique(context, "Doi", "doi",
                                            doi.substring(DOI.SCHEME.length()));
        } 
        catch (SQLException sqle) 
        {
            throw new RuntimeException(sqle);
        }
        if(null == doiRow)
        {
            throw new DOIIdentifierException("This identifier: " + identifier
                    + " isn't in our database",
                    DOIIdentifierException.DOI_DOES_NOT_EXIST);
        }
        if (TO_BE_DELETED != doiRow.getIntColumn("status")) 
        {
            log.error("This identifier: {} couldn't be deleted. "
                    + "Delete it first from metadata.", 
                    DOI.SCHEME + doiRow.getStringColumn("doi"));
            throw new IllegalArgumentException("Couldn't delete this identifier:"
                                             + DOI.SCHEME + doiRow.getStringColumn("doi")
                                             + ". Delete it first from metadata.");
        }
            connector.deleteDOI(context, doi);
            
            doiRow.setColumn("status", DELETED);
            try {
                DatabaseManager.update(context, doiRow);
                context.commit();
            }
            catch (SQLException sqle)
            {
                log.warn("SQLException while changing status of DOI {} deleted.", doi);
                throw new RuntimeException(sqle);
            }
    }
     
    /**
     * Returns a DSpaceObject depending on its DOI.
     * @param context the context
     * @param identifier The DOI in a format that is accepted by
     *                   {@link formatIdentifier(String)}.
     * @return Null if the DOI couldn't be found or the associated DSpaceObject.
     * @throws SQLException
     * @throws IdentifierException If {@code identifier} is null or an empty string.
     * @throws IllegalArgumentException If the identifier couldn't be recognized as DOI.
     */
    public static DSpaceObject getObjectByDOI(Context context, String identifier)
            throws SQLException, DOIIdentifierException, IllegalArgumentException
    {
        String doi = DOI.formatIdentifier(identifier);
        TableRow doiRow = DatabaseManager.findByUnique(context, "Doi", "doi",
                doi.substring(DOI.SCHEME.length()));
        
        if (null == doiRow)
        {
            return null;
        }
        
        if (doiRow.isColumnNull("resource_type_id") ||
                doiRow.isColumnNull("resource_id"))
        {
            log.error("Found DOI " + doi +
                    " in database, but no assigned Object could be found.");
            throw new IllegalStateException("Found DOI " + doi +
                    " in database, but no assigned Object could be found.");
        }
        
        return DSpaceObject.find(context,
                doiRow.getIntColumn("resource_type_id"),
                doiRow.getIntColumn("resource_id"));
    }
    
    /**
     * Search the database for a DOI, using the type and id of an DSpaceObject.
     *
     * @param context
     * @param dso DSpaceObject to find doi for. DOIs with status TO_BE_DELETED will be
     * ignored.
     * @return The DOI as String or null if DOI was not found.
     * @throws SQLException
     */
    public static String getDOIByObject(Context context, DSpaceObject dso)
            throws SQLException
    {
        String sql = "SELECT * FROM Doi WHERE resource_type_id = ? " +
                "AND resource_id = ? AND ((status != ? AND status != ?) OR status IS NULL)";

        TableRow doiRow = DatabaseManager.querySingleTable(context, "Doi", sql,
                dso.getType(), dso.getID(), DOIIdentifierProvider.TO_BE_DELETED,
                DOIIdentifierProvider.DELETED);
        if (null == doiRow)
        {
            return null;
        }

        if (doiRow.isColumnNull("doi"))
        {
            log.error("A DOI with an empty doi column was found in the database. DSO-Type: "
                    + dso.getTypeText() + ", ID: " + dso.getID() + ".");
            throw new IllegalStateException("A DOI with an empty doi column " +
                    "was found in the database. DSO-Type: " + dso.getTypeText() + 
                    ", ID: " + dso.getID() + ".");
        }
        
        return DOI.SCHEME + doiRow.getStringColumn("doi");
    }
    
    /**
     * Load a DOI from the database or creates it if it does not exist. This
     * method can be used to ensure that a DOI exists in the database and to
     * load the appropriate TableRow. As protected method we don't check if the
     * DOI is in a decent format, use DOI.formatIdentifier(String) if necessary.
     * 
     * @param context
     * @param dso The DSpaceObject the DOI should be loaded or created for.
     * @param doi A DOI or null if a DOI should be generated. The generated DOI
     * can be found in the appropriate column for the TableRow.
     * @return The database row of the object.
     * @throws SQLException In case of an error using the database.
     * @throws DOIIdentifierException If {@code doi} is not part of our prefix or
     *                             DOI is registered for another object already.
     */
    protected TableRow loadOrCreateDOI(Context context, DSpaceObject dso, String doi)
            throws SQLException, DOIIdentifierException
    {
        TableRow doiRow = null;
        if (null != doi)
        {
            // we expect DOIs to have the DOI-Scheme except inside the doi table:
            doi = doi.substring(DOI.SCHEME.length());
            
            // check if DOI is already in Database
            doiRow = DatabaseManager.findByUnique(context, "Doi", "doi", doi);
            if (null != doiRow)
            {
                // check if DOI already belongs to dso
                if (doiRow.getIntColumn("resource_id") == dso.getID() &&
                        doiRow.getIntColumn("resource_type_id") == dso.getType())
                {
                    return doiRow;
                }
                else
                {
                    throw new DOIIdentifierException("Trying to create a DOI " +
                            "that is already reserved for another object.",
                            DOIIdentifierException.DOI_ALREADY_EXISTS);
                }
            }

            // check prefix
            if (!doi.startsWith(this.getPrefix() + "/"))
            {
                throw new DOIIdentifierException("Trying to create a DOI " +
                        "that's not part of our Namespace!",
                        DOIIdentifierException.FOREIGN_DOI);
            }
            // prepare new doiRow
            doiRow = DatabaseManager.create(context, "Doi");
        }
        else
        {
            // Agregado por sedici
            if (dso.getHandle() == null) {
                throw new IllegalStateException("Trying to assign a DOI "
                        + "to an item without a handle!");
            }
            // We need to generate a new DOI.
            doiRow = DatabaseManager.create(context, "Doi");

            doi = this.getPrefix() + "/" + dso.getHandle();
        }
                    
        doiRow.setColumn("doi", doi);
        doiRow.setColumn("resource_type_id", dso.getType());
        doiRow.setColumn("resource_id", dso.getID());
        doiRow.setColumnNull("status");
        if (0 == DatabaseManager.update(context, doiRow))
        {
            throw new RuntimeException("Cannot save DOI to databse for unkown reason.");
        }
        
        return doiRow;
    }
    
    /**
     * Loads a DOI out of the metadata of an DSpaceObject.
     * @param dso
     * @return The DOI or null if no DOI was found.
     */
    public static String getDOIOutOfObject(DSpaceObject dso)
            throws DOIIdentifierException {
        // FIXME
        if (!(dso instanceof Item))
        {
            throw new IllegalArgumentException("We currently support DOIs for "
                    + "Items only, not for " + dso.getTypeText() + ".");
        }
        Item item = (Item)dso;

        Metadatum[] metadata = item.getMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null);
        for (Metadatum id : metadata)
        {
            if (id.value.startsWith(DOI.RESOLVER + "/10."))
            {
                return DOI.DOIFromExternalFormat(id.value);
            }
        }
        return null;
    }
    
    /**
     * Adds a DOI to the metadata of an item.
     * 
     * @param context
     * @param dso DSpaceObject the DOI should be added to.
     * @param doi The DOI that should be added as metadata.
     * @throws SQLException
     * @throws AuthorizeException 
     */
    protected void saveDOIToObject(Context context, DSpaceObject dso, String doi)
            throws SQLException, AuthorizeException, IdentifierException
    {
        // FIXME
        if (!(dso instanceof Item))
        {
            throw new IllegalArgumentException("We currently support DOIs for "
                    + "Items only, not for " + dso.getTypeText() + ".");
        }
        Item item = (Item) dso;

        item.addMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null, DOI.DOIToExternalForm(doi));
        try
        {
            item.update();
            context.commit();
        } catch (SQLException ex) {
            throw ex;
        } catch (AuthorizeException ex) {
            throw ex;
        }
    }
    
    /**
     * Removes a DOI out of the metadata of a DSpaceObject.
     * 
     * @param context
     * @param dso The DSpaceObject the DOI should be removed from.
     * @param doi The DOI to remove out of the metadata.
     * @throws AuthorizeException
     * @throws SQLException 
     */
    protected void removeDOIFromObject(Context context, DSpaceObject dso, String doi)
            throws AuthorizeException, SQLException, IdentifierException
    {
        // FIXME
        if (!(dso instanceof Item))
        {
            throw new IllegalArgumentException("We currently support DOIs for "
                    + "Items only, not for " + dso.getTypeText() + ".");
        }
        Item item = (Item)dso;

        Metadatum[] metadata = item.getMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null);
        List<String> remainder = new ArrayList<String>();

        for (Metadatum id : metadata)
        {
            if (!id.value.equals(DOI.DOIToExternalForm(doi)))
            {
                remainder.add(id.value);
            }
        }

        item.clearMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null);
        item.addMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null,
                remainder.toArray(new String[remainder.size()]));
        try {
            item.update();
            context.commit();
        } catch (SQLException e) {
            throw e;
        } catch (AuthorizeException e) {
            throw e;
        }
    }
}
