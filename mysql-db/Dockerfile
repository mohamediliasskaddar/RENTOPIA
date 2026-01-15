FROM mysql:8.0

ENV MYSQL_DATABASE=rental_db
ENV MYSQL_ROOT_PASSWORD=44010904mysql

COPY init.sql /docker-entrypoint-initdb.d/
EXPOSE 3306
