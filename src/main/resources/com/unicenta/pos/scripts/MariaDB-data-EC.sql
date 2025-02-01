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

-- ADD IDENTIFICATION TYPES FOR ECUADOR
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('C', 'Cédula', '05', 10, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('R', 'RUC', '04', 13, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('P', 'Pasaporte', '06', 0, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('CF', 'Consumidor Final', '07', 0, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('IE', 'Identificación del Exterior', '08', 0, 'EC');

-- ADD Consumidor Final
INSERT INTO customers (id,searchkey,taxid,name,maxdebt,address,address2,taxid_type,firstname,lastname,notes,visible,isvip,discount) 
VALUES ('9999999999999','9999999999999','9999999999999','Consumidor Final',49.99,NULL,NULL,'CF','Consumidor','Final','',1,0,0);

Insert into ele_parameters (ID,name,value,observation,type) 
values (0,'Base Directory','/app/RoQui','Base directory for files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (1,'Generated','/app/RoQui/receipt/generated','URL generated xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (2,'Signed','/app/RoQui/receipt/signed','URL signed xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (3,'Sended','/app/RoQui/receipt/sended','URL sended xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (4,'Authorized','/app/RoQui/receipt/authorized','URL authorized xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (5,'Unauthorized','/app/RoQui/receipt/unauthorized','URL unauthorized xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (6,'Refused','/app/RoQui/receipt/refused','URL refused xml files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (7,'PDF','/app/RoQui/receipt/pdf','URL pdf files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (8,'Certificate','/app/RoQui/Certificate/Certificate.p12','URL certificate','Certificate');
Insert into ele_parameters (ID,name,value,observation,type) 
values (9,'Certificate Password','***************==','Certificate Password','Certificate');
Insert into ele_parameters (ID,name,value,observation,type) 
values (10,'Logo','/app/RoQui/Resources/images/logo.jpeg','URL logo','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (99,'Subscription','**************==','Subscription','Subscription');
