--    Don POS - Touch Friendly Point Of Sale
--    https://joguenco.dev
--
--    This file is part of Don POS.
--
--    Don POS is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    Don POS is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

-- Customer default
INSERT INTO resources(id, name, restype, content) VALUES('90', 'Customer.Default', 0, $FILE{/com/unicenta/pos/templates/Customer.Default.CO.txt});

-- ADD IDENTIFICATION TYPES FOR COLOMBIA
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('0', 'Consumidor Final', '00', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('1', 'Registro civil', '11', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('2', 'Tarjeta de identidad', '12', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('3', 'Cédula de ciudadanía', '13', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('4', 'Tarjeta de extranjería', '21', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('5', 'Cédula de extranjería', '22', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('6', 'NIT', '31', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('7', 'Pasaporte', '41', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('8', 'Documento de identificación extranjero', '42', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('9', 'NIT de otro país', '50', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('10', 'NUIP *', '91', 0, 'CO');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('11', 'PEP', '47', 0, 'CO');
				
INSERT INTO customers (id,searchkey,taxid,name,maxdebt,address,address2,taxid_type,firstname,lastname,notes,visible,isvip,discount) 
VALUES ('222222222222','222222222222','222222222222','Consumidor Final',50,'Bogota',NULL,'0','Final','Consumidor','',1,0,0);
