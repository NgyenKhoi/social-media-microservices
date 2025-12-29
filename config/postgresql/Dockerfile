FROM postgres:17

COPY 01-init-databases.sh /docker-entrypoint-initdb.d/01-init-databases.sh

#  CRLF khiến cho việc run script từ windows sang linux container bị lỗi vì CRLF làm nó sai đường dẫn file -> không tìm thấy file sh để run.
#  FIX CRLF → LF BÊN TRONG IMAGE
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/01-init-databases.sh \
    && chmod +x /docker-entrypoint-initdb.d/01-init-databases.sh