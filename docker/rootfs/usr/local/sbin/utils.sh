#!/bin/bash
set -e


#########################################################
########## GENERAL HELPER FUNCTIONS #####################
#########################################################

# @param 1 (optional) Confirmation message
# @returns user answer 0=yes, -1=no
confirm() {
    local message=${1:-'Are you sure? [y/N]'}
	# call with a prompt string or use a default
    read -r -p "$message" response
	response=${response,,} 
    if [[ $response =~ ^(yes|y|Y) ]]; then
		return 0
    else
        return -1
    fi
}

print_err(){
    echo -e "\n===================================================="
	printf "[ERROR] %s\n" "$*" >&2;
    echo -e "\n===================================================="
    exit 1;
}

print_warn(){
	printf "[WARN] %s\n" "$*";
}

print_info(){
	printf "[INFO] %s\n" "$*";
}

# @param 1 (required) property name to look for
# @param 2 (required) java properties file name 
# @returns property value if found, null otherwise
get_dspace_property () {
	local cfg_file=${2}
    grep "^${1}\s*\=" ${cfg_file} |cut -d'=' -f2
}

# add the property=value assignment in a java properties file or override it if already exists
# @param 1 (required) property name
# @param 2 (required) property value 
# @param 3 (required) java properties  file name 
set_dspace_property (){
	local propval="${1} = ${2}"
	local cfg_file=${3}
	if [[ ! -f "$cfg_file" ]]; then
		print_warn "El archivo ${cfg_file} no existe, no se puede continuar"
		touch $cfg_file

	fi
	local oldval=$(get_dspace_property $1 $cfg_file)
	if [ -z "${oldval}" ]; then
        echo $propval >> ${cfg_file}
	else
        sed -i "s#^${1}.\?=.*#${propval}#" $cfg_file
    fi
}
#########################################################
######### POSTGRES HELPER FUNCTIONS #####################
#########################################################

# executes a given command in postgres using context credentials 
# @param 1 (required) command
# @param 2 (optional) in case more parameters are added
# @returns psql return code
run_pg(){

        export PGUSER="${POSTGRES_DB_USER}"
        export PGPASSWORD="${POSTGRES_DB_PASS}"
        extra_params=${2}
        psql -h ${POSTGRES_DB_HOST} -p ${POSTGRES_DB_PORT} -d ${POSTGRES_DB_NAME} -c "${1}" ${extra_params}

		result=$?
        unset PGPASSWORD
        unset PGUSER

		return $result
}

# Test db connection using context credentials and exit on failure
test_db_connection (){
	local canconnect=$(run_pg "\\connect")
	if [[ ! $? -eq 0 ]]; then
		print_err "PSQL connection error: Could not connect using HOST=$POSTGRES_DB_HOST PORT=$POSTGRES_DB_PORT DBNAME=$POSTGRES_DB_NAME USER=$PGUSER and PGPASSWORD=$PGPASSWORD"
	fi
}

# Enable pg_crypto if is not enabled 
enable_pg_crypto()
{
        isPgCryptoInstalled=$(run_pg "\dx pgcrypto" )
		if [[ ! $? -eq 0 ]]; then
            print_err "PSQL connection error: Cannot connect using HOST=$POSTGRES_DB_HOST PORT=$POSTGRES_DB_PORT DBNAME=$POSTGRES_DB_NAME USER=$PGUSER and PGPASSWORD=$PGPASSWORD"
        fi

        if [[ ! $(echo $isPgCryptoInstalled | grep pgcrypto | wc -l) -eq 1 ]]; then
                print_warn "pgcrypto not installed in database"
                wasPgCryptoInstalled=$(run_pg "CREATE EXTENSION pgcrypto;")

                if [[ ! $? -eq 0 ]]; then
                	print_err "PSQL connection error: Cannot create extension PGCRYPTO"
                fi
                print_info "OK pgcrypto extension created"
        else
			print_info "OK pgcrypto extension is available"
		fi
}

restore_db () {
	print_info 'Searching for dump files...'
	if [ -f $BOOTSTRAP_DUMP ]; then
        run_pg "" "-f $BOOTSTRAP_DUMP"
        if [[ ! $? -eq 0 ]]; then
            print_err "PSQL connection error: Cannot restore database"
        fi
    fi

}

#########################################################
#########################################################
