#!/bin/bash
set -e

source utils.sh

# Init script environment
init_env () {
	#looks for POSTGRES ENV VARS 
	if [ ! -z $DB_PORT ]; then
		# DB_PORT is something like tcp://127.0.0.4:5432/
		POSTGRES_DB_HOST=`echo $DB_PORT | cut -d / -f 3 | cut -d \: -f 1`
		POSTGRES_DB_PORT=`echo $DB_PORT | cut -d / -f 3 | cut -d \: -f 2`
		POSTGRES_DB_NAME=${DB_ENV_POSTGRES_DB:-$POSTGRES_DB_NAME}
		POSTGRES_DB_USER=${DB_ENV_POSTGRES_USER:-$POSTGRES_DB_NAME}
		POSTGRES_DB_PASS=${DB_ENV_POSTGRES_PASSWORD:-$POSTGRES_DB_PASS}
	fi

	# export 
	export DSPACE_SOURCE=$DSPACE_BASE
	export DSPACE_DIR=$DSPACE_BASE/install

	SOURCE_CFG_FILENAME="$DSPACE_SOURCE/dspace/config/local.cfg"
	INSTALL_CFG_FILENAME="$DSPACE_DIR/config/local.cfg"

	TOMCAT="${CATALINA_HOME}/bin/catalina.sh"
}

init_sources()
{
	# download sources
#	git clone -v --progress --depth=1 --branch "${DSPACE_GIT_REVISION}" "${DSPACE_GIT_URL}"  $DSPACE_SOURCE

	#creates local.cfg if it does not exist
	if [[ ! -f "$SOURCE_CFG_FILENAME" ]]; then
		cp "$SOURCE_CFG_FILENAME.EXAMPLE" "$SOURCE_CFG_FILENAME"
	fi
	reset_permissions
}

# Init DSpace local.cfg with contextual settings
init_config() {
	local cfg_file=${1}

	set_dspace_property "db.url" "jdbc:postgresql://${POSTGRES_DB_HOST}:${POSTGRES_DB_PORT}/${POSTGRES_DB_NAME}" $cfg_file
	set_dspace_property "db.username" "${POSTGRES_DB_USER}" $cfg_file
	set_dspace_property "db.password" "${POSTGRES_DB_PASS}" $cfg_file
	set_dspace_property "dspace.dir" "${DSPACE_DIR}" $cfg_file
	
	#this allows truncating the database
	set_dspace_property "db.cleanDisabled" "false" $cfg_file
}

reset_db (){
    dspace database clean

    if [ ! -f $BOOTSTRAP_DUMP ]; then
        dspace database migrate
    else
        restore_db
    fi
}

truncate_all (){
	
	init_config $INSTALL_CFG_FILENAME
	if ( confirm "Esta por borrar el directorio de instalación de dspace $DSPACE_DIR y la base de datos, está seguro que desea hacerlo? [Y/n]" ); then
		print_info "Hago el clean y migrate de la BD para limpiarla. Si falla al crear el admin es porque el migrate no esta creando el group admin"
		#TODO check if dspace cmd exists
		dspace database clean

		rm -rf $DSPACE_DIR/*
	else
		print_err "Operación cancelada."
	fi
}

rebuild_installer(){

	init_config $SOURCE_CFG_FILENAME
#	print_info "Packaging dspace with MAVEN_OPTS='$MAVEN_OPTS'. "
	print_info "Packaging dspace. "
	print_info "Please be patient, it may take several minutes. "

	sudo --login -u $DSPACE_USER <<EOF
	cd $DSPACE_SOURCE
	
	#source ~/.bashrc

	#mvn package $MAVEN_OPTS
	mvn package

EOF

}

reset_permissions(){

	chown -R $DSPACE_USER.$DSPACE_USER $DSPACE_BASE

}
enable_webapps(){
	print_info "Creating symlinks for webapps"

	#delete symlinks if exist
	if [ ! -z "`ls -A $CATALINA_HOME/webapps/`" ]; then
		rm $CATALINA_HOME/webapps/* 
	fi

	#enable ROOT Webapp
	root_wa="$DSPACE_ROOT_WEBAPP"
	if [ ! -z "$root_wa" ]; then
		#TODO test existence of $DSPACE_DIR/webapps/$root_wa
		ln -s $DSPACE_DIR/webapps/$root_wa $CATALINA_HOME/webapps/ROOT
	fi

	#enable all webapps
	for wa in $DSPACE_DIR/webapps/*
	do 
		if [ "$root_wa" != "$wa" ]; then
			ln -s $wa $CATALINA_HOME/webapps/$(basename $wa)
		fi
	done

	print_info "Se activaron las siguientes webapps: `ls $CATALINA_HOME/webapps/`"
}

#########################################################
#########################################################
start () {
	
	if [ ! -d $DSPACE_DIR ]; then
		print_err  "El directorio de instalación ${DSPACE_DIR} no existe, debe instalar!"
	fi
	
	init_config $INSTALL_CFG_FILENAME
	test_db_connection

	enable_webapps
    if [ "$1" = "--debug" ]; then
        $TOMCAT jpda run
    else
        $TOMCAT run
    fi
}

install (){

	if [ -d $DSPACE_DIR ]; then
		if ( confirm "Previous DSpace installation found. Desea reinstalar? [Y/n]"); then
			truncate_all
		else
			print_err "Instalación cancelada"
		fi
	fi

	if [ ! -d $DSPACE_SOURCE ]; then
		init_sources
	fi


	rebuild_installer

	enable_pg_crypto
    restore_db

	cd $DSPACE_SOURCE/dspace/target/dspace-installer
	ant fresh_install

	print_info "Creating admin user"
	dspace create-administrator 
	#--email ${ADMIN_EMAIL} --first DSpace --last Administrator --language es --password -${ADMIN_PASSWD} 

	#re? enable all webapps
	enable_webapps

	# do no init discovery index as solr is required and is not available
	# dspace index-discovery
}

update () 
{
	# $TOMCAT stop > /dev/null
	rebuild_installer $1
	cd $DSPACE_SOURCE/dspace/target/dspace-installer
	ant clean_backups update
	cd $DSPACE_SOURCE 
	mvn clean
	# $TOMCAT start > /dev/null
}

usage() {
	#TODO UPDATE MSG
	echo "     - install"
	echo "     - truncate"
	echo "     - update"
	echo "     - update-fast: build inside dspace dir (compiles only customizations)"
	echo "     - start"
	echo "     - start --debug: enable remote debug mode"
	echo "     - reset-db"
	exit 1
}

just_wait() 
{
	tail -n 10 ${DSPACE_DIR}/log/dspace.log
	#tail -F /etc/hosts
	#-l "tail -F ${CATALINA_HOME}/logs/catalina.out" -l "tail -F ${DSPACE_DIR}/log/dspace.log"
}
#########################################################
#########################################################

#validates current user be the same as DSPACE_USER
id -u ${DSPACE_USER} &>/dev/null || useradd --home-dir $DSPACE_BASE --create-home --shell /bin/bash $DSPACE_USER

cd $DSPACE_BASE

init_env
reset_permissions
case "$1" in
  	start)
        if [ "$2" = "--debug" ]; then
           start "--debug"
        else
           start
        fi
		just_wait
		;;
  	install)
		install
		;;
  	update)
		update
        ;;
  	update-fast)
		update fast
        ;;
    reset-db)
        reset_db    
        ;;
  	*)
        usage
        ;;
esac
exit 0
