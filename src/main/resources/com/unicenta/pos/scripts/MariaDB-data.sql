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
INSERT INTO resources(id, name, restype, content) VALUES('90', 'Customer.Default', 0, $FILE{/com/unicenta/pos/templates/Customer.Default.txt});

INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('V', 'VAT', '01', 0, $COUNTRY_CODE{});

-- ADD TAXCATEGORIES
/* 002 added 31/01/2017 00:00:00. */
INSERT INTO taxcategories(id, name) VALUES ('000', 'Tax Exempt');
INSERT INTO taxcategories(id, name) VALUES ('001', 'Tax Standard');
INSERT INTO taxcategories(id, name) VALUES ('002', 'Tax Other');

-- ADD TAXES
/* 002 added 31/01/2017 00:00:00. */
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('000', 'Tax Exempt', '000', NULL, NULL, 0, FALSE, NULL, '0');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('001', 'Tax Standard', '001', NULL, NULL, 0.20, FALSE, NULL, '0');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('002', 'Tax Other', '002', NULL, NULL, 0, FALSE, NULL, '0');

-- ADD PRODUCTS
INSERT INTO products(id, reference, code, name, category, taxcat, isservice, display, printto) 
VALUES ('xxx999_999xxx_x9x9x9', 'xxx999', 'xxx999', 'Free Line entry', '000', '001', 1, '<html><center>Free Line entry', '1');
INSERT INTO products(id, reference, code, name, category, taxcat, isservice, display, printto) 
VALUES ('xxx998_998xxx_x8x8x8', 'xxx998', 'xxx998', 'Service Charge', '000', '001', 1, '<html><center>Service Charge', '1');

-- ADD PRODUCTS_CAT
INSERT INTO products_cat(product) VALUES ('xxx999_999xxx_x9x9x9');
INSERT INTO products_cat(product) VALUES ('xxx998_998xxx_x8x8x8');

-- ADD LOCATION
INSERT INTO locations(id, name, address) VALUES ('0','Location 1','Local');

-- ADD SUPPLIERS
INSERT INTO suppliers(id, searchkey, name) VALUES ('0','uniCenta','uniCenta');

-- ADD UOM
INSERT INTO uom(id, name) VALUES ('0','Each');

INSERT INTO categories(id, name) VALUES ('001', 'Category Local');
INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
VALUES ('1', '1', '1', 'Test Product', 1, '001', '001', 0, '<html><center>Test Product', '1', '0');
INSERT INTO products_cat(product) VALUES ('1');

INSERT INTO customers (id,searchkey,taxid,name,maxdebt,address,address2,taxid_type,firstname,lastname,notes,visible,isvip,discount) 
VALUES ('000000000','000000000','000000000','Consumidor Final',50,'My Home',NULL,'V','Final','Consumidor','',1,0,0);
