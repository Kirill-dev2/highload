#!/bin/bash

docker stop backend-service

docker exec -it db-master psql -U postgres -c "CREATE ROLE repluser WITH login replication password 'replpass';"
docker exec -it db-master bash -c "echo 'wal_level = replica
max_wal_senders = 4
max_replication_slots = 4
wal_keep_size = 32
hot_standby = on
hot_standby_feedback' = on >> /var/lib/postgresql/data/postgresql.conf"

subnet=$(docker inspect highload_default -f '{{range .IPAM.Config}}{{.Subnet}}{{end}}')
docker exec -it db-master bash -c "echo 'host    replication     all             ${subnet}        trust' >> /var/lib/postgresql/data/pg_hba.conf"
docker restart db-master

sleep 5

docker exec -it db-slave bash -c "rm -r /var/lib/postgresql/data/*"
docker exec -it db-slave bash -c "pg_basebackup --host=db-master --username=repluser --pgdata=/var/lib/postgresql/data --wal-method=stream --write-recovery-conf"

docker exec -it db-slave-2 bash -c "rm -r /var/lib/postgresql/data/*"
docker exec -it db-slave-2 bash -c "pg_basebackup --host=db-master --username=repluser --pgdata=/var/lib/postgresql/data --wal-method=stream --write-recovery-conf"

docker stop db-master