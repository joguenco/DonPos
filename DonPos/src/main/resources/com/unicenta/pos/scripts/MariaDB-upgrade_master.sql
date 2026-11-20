INSERT INTO resources(id, name, restype, content) VALUES('112', 'Email.Provider', 0, $FILE{/com/unicenta/pos/templates/Email.Provider.xml});

UPDATE applications SET version = $APP_VERSION{} WHERE id = $APP_ID{};
COMMIT;
