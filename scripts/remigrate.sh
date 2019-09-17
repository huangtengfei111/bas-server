#!/bin/sh
read -p "Are you sure to DROP database and re-create database from beginning? [y|n] " -n 1 -r
echo    # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]
  mvn db-migrator:drop
  mvn db-migrator:create && mysql -uadmin bas_deve < ./src/migrations/procedures/AddColumnUnlessExists.sql && mysql -uadmin bas_test < ./src/migrations/procedures/AddColumnUnlessExists.sql && mvn db-migrator:migrate
then
    [[ "$0" = "$BASH_SOURCE" ]] && exit 1 || return 1 # handle exits from shell or function but don't exit interactive shell
fi
