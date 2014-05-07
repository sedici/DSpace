// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.web;

import ar.edu.unlp.sedici.sedici2003.model.Contacto;
import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.String;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

privileged aspect ContactoController_Roo_Controller {
    
    @RequestMapping(method = RequestMethod.POST)
    public String ContactoController.create(@Valid Contacto contacto, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("contacto", contacto);
            addDateTimeFormatPatterns(uiModel);
            return "contactoes/create";
        }
        uiModel.asMap().clear();
        contacto.persist();
        return "redirect:/contactoes/" + encodeUrlPathSegment(contacto.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String ContactoController.createForm(Model uiModel) {
        uiModel.addAttribute("contacto", new Contacto());
        addDateTimeFormatPatterns(uiModel);
        return "contactoes/create";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String ContactoController.show(@PathVariable("id") Integer id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("contacto", Contacto.findContacto(id));
        uiModel.addAttribute("itemId", id);
        return "contactoes/show";
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String ContactoController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("contactoes", Contacto.findContactoEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Contacto.countContactoes() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("contactoes", Contacto.findAllContactoes());
        }
        addDateTimeFormatPatterns(uiModel);
        return "contactoes/list";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String ContactoController.update(@Valid Contacto contacto, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("contacto", contacto);
            addDateTimeFormatPatterns(uiModel);
            return "contactoes/update";
        }
        uiModel.asMap().clear();
        contacto.merge();
        return "redirect:/contactoes/" + encodeUrlPathSegment(contacto.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String ContactoController.updateForm(@PathVariable("id") Integer id, Model uiModel) {
        uiModel.addAttribute("contacto", Contacto.findContacto(id));
        addDateTimeFormatPatterns(uiModel);
        return "contactoes/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String ContactoController.delete(@PathVariable("id") Integer id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Contacto.findContacto(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/contactoes";
    }
    
    @ModelAttribute("contactoes")
    public Collection<Contacto> ContactoController.populateContactoes() {
        return Contacto.findAllContactoes();
    }
    
    void ContactoController.addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("contacto_fechahoraenvio_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }
    
    String ContactoController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    
}