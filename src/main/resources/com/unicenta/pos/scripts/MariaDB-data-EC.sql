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

CREATE TABLE `ele_parameters` (
  `id` bigint(18) NOT NULL,
  `name` varchar(300) DEFAULT NULL,
  `value` varchar(300) DEFAULT NULL,
  `observation` varchar(300) DEFAULT NULL,
  `type` varchar(300) DEFAULT NULL,
  `status` boolean DEFAULT true,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `ele_documents` (
    `id` BIGINT(18) NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(18) NOT NULL,
    `number` VARCHAR(18) NOT NULL,
    `authorization` VARCHAR(90) DEFAULT NULL,
    `authorization_date` DATETIME DEFAULT NULL,
    `observation` VARCHAR(3000) DEFAULT NULL,
    `status` VARCHAR(30) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ele` (`code` , `number`) USING BTREE,
    KEY `inx_fecha` (`code` , `number` , `status`) USING BTREE
)  ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE VIEW `v_ele_taxpayer` AS SELECT 
        `id`,
        `identification`,
        `legal_name`,
        `forced_accounting`,
        `special_taxpayer`,
        `retention_agent`,
        `other`
    FROM
        `taxpayer`;

CREATE VIEW `v_ele_establishments` as SELECT 
    (cast(`t`.`id` as unsigned)) AS `id`,
    `t`.`identification`,
    `e`.`id` AS `code`,
    `e`.`comercial_name` as `business_name`,
    `e`.`address`,
    `e`.`principal`
from
    `taxpayer` `t` join `establishments` `e`;

CREATE VIEW `v_ele_invoices` AS select
    (cast(ROWNUM() as unsigned)) AS `id`,
    `t`.`code` AS `code`,
    `t`.`serie_number` AS `number`,
    cast('01' as char) AS `code_document`,
    substr(`t`.`serie_number`, 1, 3) AS `establishment`,
    substr(`t`.`serie_number`, 4, 3) AS `emission_point`,
    cast(lpad(`t`.`ticketid`, 9, '0') as char) AS `sequence`,
    cast(`r`.`datenew` as date) AS `date`,
    round(sum(cast(`tl`.`units` * `tl`.`price` as decimal(19, 2))), 2) as `total_without_taxes`,
    round(sum(cast(`tl`.`units` * `tl`.`price` + if(`tx`.`rate` > 0, `tl`.`units` * `tl`.`price` * `tx`.`rate`, 0) as decimal(19, 2))), 2) AS `total`,
    `i`.`legal_code` AS `identification_type`,
    `c`.`taxid` AS `identification`,
    `c`.`name` AS `legal_name`,
    `c`.`address` AS `address`,
    cast(NULL as char(20)) AS `delivery_note`,
    (
    select
        `e`.`address`
    from
        `establishments` `e`
    where
        `e`.`id` = substr(`t`.`serie_number`, 1, 3)) AS `establishment_address`,
    `t`.`access_key` as `access_key`
from
    (`tickets` `t`
join `receipts` `r` on
    `t`.`id` = `r`.`id`
join `customers` `c` on
    `c`.`id` = `t`.`customer`
join `identification_type` `i` on
    `i`.`code` = `c`.`taxid_type`
join `ticketlines` `tl` on
    `t`.`id` = `tl`.`ticket`
join `taxes` `tx` on
    `tx`.`category` = `tl`.`taxid`)
where
    (`t`.`code` = 'FV')
    and (`t`.`tickettype` = 0)
group by
    (cast(`t`.`ticketid` as unsigned)),
    `t`.`code`,
    `t`.`serie_number`,
    cast('01' as char),
    substr(`t`.`serie_number`, 1, 3),
    substr(`t`.`serie_number`, 4, 3),
    cast(lpad(`t`.`ticketid`, 9, '0') as char),
    cast(`r`.`datenew` as date),
    `i`.`legal_code`,
    `c`.`taxid`,
    `c`.`name`,
    `c`.`address`,
    cast(NULL as char(20)),
    cast('' as char(10));

CREATE VIEW `v_ele_invoices_detail` as SELECT
    (CAST(CONCAT(`t`.`TICKETID`, `tl`.`LINE`) AS UNSIGNED)) AS `id`,
    t.code,
    `t`.`serie_number` AS `number`,
    `p`.`REFERENCE` AS `principal_code`,
    CAST(`tl`.`line` AS UNSIGNED) AS `line`,
    `p`.`name`,
    CAST(`tl`.`units` AS DECIMAL (19 , 2 )) AS `quantity`,
    CONVERT('UN', CHAR) AS `unit`,
    CAST(`tl`.`PRICE` AS DECIMAL (19 , 2 )) AS `unit_price`,
    CONVERT(`tx`.`legalcode`, CHAR) AS `tax_code`,
    CAST((`tx`.`RATE` * 100) AS DECIMAL (19 , 2 )) AS `tax_iva`,
    CAST(((`tl`.`UNITS` * `tl`.`PRICE`) * `tx`.`RATE`)
        AS DECIMAL (19 , 2 )) AS `value_iva`,
    CAST(0 AS DECIMAL (19 , 2 )) AS `discount`,
    CAST((`tl`.`UNITS` * `tl`.`PRICE`) AS DECIMAL (19 , 2 )) AS `total_price_without_tax`
FROM
    (((`tickets` `t`
    JOIN `ticketlines` `tl` ON ((`t`.`ID` = `tl`.`TICKET`)))
    JOIN `taxes` `tx` ON ((`tx`.`CATEGORY` = `tl`.`TAXID`)))
    JOIN `products` `p` ON ((`p`.`ID` = `tl`.`PRODUCT`)))
WHERE
    (`t`.`TICKETTYPE` = 0);

CREATE VIEW `v_ele_taxes_detail` as SELECT 
        (CAST(CONCAT(`t`.`TICKETID`, `tl`.`LINE`, '2') AS UNSIGNED)) AS `id`,
        t.code AS `code`,
        `t`.`serie_number` AS `number`,
        `p`.`REFERENCE` AS `principal_code`,
        CAST(`tl`.`LINE` AS UNSIGNED) AS `line`,
        CONVERT( '2' , CHAR) AS `tax_code`,
        CONVERT( `tx`.`legalcode` , CHAR) AS `percentage_code`,
        ABS(CAST((`tl`.`UNITS` * `tl`.`PRICE`) AS DECIMAL (19 , 2 ))) AS `tax_base`,
        CAST((`tx`.`RATE` * 100) AS DECIMAL (19 , 2 )) AS `tax_iva`,
        ABS(CAST(((`tl`.`UNITS` * `tl`.`PRICE`) * `tx`.`RATE`)
                    AS DECIMAL (19 , 2 ))) AS `value`
    FROM
        (((`tickets` `t`
        JOIN `ticketlines` `tl` ON ((`t`.`ID` = `tl`.`TICKET`)))
        JOIN `taxes` `tx` ON ((`tx`.`CATEGORY` = `tl`.`TAXID`)))
        JOIN `products` `p` ON ((`p`.`ID` = `tl`.`PRODUCT`)));

CREATE VIEW v_ele_information as WITH information AS 
   (SELECT `TAXID` AS `identification`,
	'Email' AS `name`,
	`EMAIL` AS `value`
FROM `customers`
WHERE
	((`EMAIL` IS NOT NULL)
		AND (`EMAIL` <> ''))
UNION all SELECT
	`TAXID`,
	'Dirección',
	`ADDRESS`
FROM
	`customers`
WHERE
	((`ADDRESS` IS NOT NULL)
		AND (`ADDRESS` <> ''))
UNION all SELECT
	`TAXID`,
	'Teléfono',
	`PHONE`
FROM
	`customers`
WHERE
	((`PHONE` IS NOT NULL)
		AND (`PHONE` <> ''))
		)
SELECT ROWNUM() AS `id`,
	`identification`,
	`name`,
	`value` FROM information;

CREATE  VIEW `v_ele_payments` AS SELECT ROWNUM() `id`,
        `t`.`code` AS `code`,
        `t`.`serie_number` AS `number`,
        CONVERT(IF((`p`.`PAYMENT` = 'cash'), '01', '20') , CHAR) AS `way_pay`,
        CONVERT(IF((`p`.`PAYMENT` = 'cash'),
                'SIN UTILIZACION DEL SISTEMA FINANCIERO',
                'OTROS CON UTILIZACION DEL SISTEMA FINANCIERO'),
            CHAR) AS `name`,
        CAST(`p`.`total` AS DECIMAL (19, 2 )) AS `total`,
        CONVERT(NULL, DECIMAL (9)) AS `payment_deadline`,
        CONVERT(NULL, CHAR (20)) AS `unit_time`
    FROM
        ((`tickets` `t`
        JOIN `receipts` `r` ON ((`t`.`ID` = `r`.`ID`)))
        JOIN `payments` `p` ON ((`r`.`ID` = `p`.`RECEIPT`)))
    WHERE
        (`t`.`TICKETTYPE` = 0);

CREATE  VIEW `v_ele_report_invoices` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`date` AS `date`,
    `j`.`total` AS `total`,
    `j`.`identification` AS `identification`,
    `j`.`legal_name` AS `legal_name`,
    (select
        `i`.`value`
    from
        `v_ele_information` `i`
    where
        `i`.`name` = 'Email'
        and `i`.`identification` = `j`.`identification`
    limit 1) AS `email`,
    ifnull((select `e`.`status` from `ele_documents` `e` where `e`.`code` = `j`.`code` and `e`.`number` = `j`.`number`), 'NO ENVIADO') AS `status`
from
    `v_ele_invoices` `j`
order by `j`.`number` desc, `j`.`number` desc;

CREATE VIEW `v_version` AS select 1 AS `id`,
    version() AS `version_database`;

CREATE VIEW `v_users` AS select rownum() AS `id`,
    `u`.`name` AS `username`,
    `u`.`apppassword` AS `password`,
    replace(`r`.`name`, ' role', '') AS `role`,
    `u`.`visible` as `status`
from
    (`people` `u`
join `roles` `r` on
    (`u`.`role` = `r`.`id`));


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

-- ADD IDENTIFICATION TYPES FOR ECUADOR
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('C', 'Cédula', '05', 10, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('R', 'RUC', '04', 13, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('P', 'Pasaporte', '06', 0, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('CF', 'Consumidor Final', '07', 0, 'EC');
INSERT INTO identification_type(code, name, legal_code, length, country_code) VALUES ('IE', 'Identificación del Exterior', '08', 0, 'EC');

-- ADD Consumidor Final
INSERT INTO customers (id,searchkey,taxid,name,maxdebt,address,address2,taxid_type,firstname,lastname,notes,visible,isvip,discount) 
VALUES ('9999999999999','9999999999999','9999999999999','Consumidor Final',49.99,'Mi Dirección',NULL,'CF','Consumidor','Final','',1,0,0);

INSERT INTO categories(id, name) VALUES ('001', 'Category Local');

-- ADD TAXCATEGORIES
INSERT INTO taxcategories(id, name) VALUES ('000', 'IVA 0');
INSERT INTO taxcategories(id, name) VALUES ('012', 'IVA 12');
INSERT INTO taxcategories(id, name) VALUES ('008', 'IVA 8');
INSERT INTO taxcategories(id, name) VALUES ('015', 'IVA 15');
INSERT INTO taxcategories(id, name) VALUES ('013', 'IVA 13');
INSERT INTO taxcategories(id, name) VALUES ('005', 'IVA 5');

-- ADD TAXES
/* 002 added 31/01/2017 00:00:00. */
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('000', 'IVA 0', '000', NULL, NULL, 0, FALSE, NULL, '0');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('012', 'IVA 12', '012', NULL, NULL, 0.12, FALSE, NULL, '2');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('008', 'IVA 8', '008', NULL, NULL, 0.08, FALSE, NULL, '8');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('015', 'IVA 15', '015', NULL, NULL, 0.15, FALSE, NULL, '4');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('013', 'IVA 13', '013', NULL, NULL, 0.13, FALSE, NULL, '10');
INSERT INTO taxes(id, name, category, custcategory, parentid, rate, ratecascade, rateorder, legalcode) VALUES ('005', 'IVA 5', '005', NULL, NULL, 0.05, FALSE, NULL, '5');

-- ADD PRODUCTS
INSERT INTO products(id, reference, code, name, category, taxcat, isservice, display, printto) 
VALUES ('xxx999_999xxx_x9x9x9', 'xxx999', 'xxx999', 'Free Line entry', '000', '000', 1, '<html><center>Free Line entry', '1');
INSERT INTO products(id, reference, code, name, category, taxcat, isservice, display, printto) 
VALUES ('xxx998_998xxx_x8x8x8', 'xxx998', 'xxx998', 'Service Charge', '000', '000', 1, '<html><center>Service Charge', '1');

-- ADD PRODUCTS_CAT
INSERT INTO products_cat(product) VALUES ('xxx999_999xxx_x9x9x9');
INSERT INTO products_cat(product) VALUES ('xxx998_998xxx_x8x8x8');

-- ADD PRODUCTS Local
INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
VALUES ('1', '1', '1', 'Producto 0%', 1, '001', '000', 0, '<html><center>Producto 0%', '1', 'u');
-- INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
-- VALUES ('2', '2', '2', 'Producto 12%', 1, '001', '012', 0, '<html><center>Producto 12%', '1', 'u');
-- INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
-- VALUES ('3', '3', '3', 'Producto 13%', 1, '001', '013', 0, '<html><center>Producto 13%', '1', 'u');
INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
VALUES ('4', '4', '4', 'Producto 15%', 1, '001', '015', 0, '<html><center>Producto 15%', '1', 'u');
INSERT INTO products(id, reference, code, name, pricesell, category, taxcat, isservice, display, printto, uom) 
VALUES ('5', '5', '5', 'Producto 5%', 1, '001', '005', 0, '<html><center>Producto 5%', '1', 'u');


-- ADD PRODUCTS_CAT
INSERT INTO products_cat(product) VALUES ('1');
-- INSERT INTO products_cat(product) VALUES ('2');
-- INSERT INTO products_cat(product) VALUES ('3');
INSERT INTO products_cat(product) VALUES ('4');
INSERT INTO products_cat(product) VALUES ('5');

-- ADD LOCATION
INSERT INTO locations(id, name, address) VALUES ('0','Location 1','Local');

-- ADD SUPPLIERS
INSERT INTO suppliers(id, searchkey, taxid, taxid_type, name) VALUES ('9999999999999','9999999999999', '9999999999999', 'CF', 'Otros Proveedores');

-- ADD UOM
INSERT INTO uom(id, name) VALUES ('u','Unidad');

INSERT INTO subscriptions (id,name,url,token,timeout,status) 
VALUES ('1', 'ReIdi', 'https://reidi.ec.service.resolvedor.dev', 
'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJyZWlkaS5zZXJ2aWNlLmpvZ3VlbmNvLmRldiIsImlhdCI6MTc0MDM2NjM2OSwiZXhwIjoxNzU1OTE4MzY5LCJhdWQiOiJqb2d1ZW5jby5kZXYiLCJzdWIiOiJqb3JnZWx1aXNAam9ndWVuY28uZGV2IiwiY2xpZW50IjoiOTk5OTk5OTk5OTk5OSIsIm5hbWUiOiJKb3JnZSBMdWlzIiwiZW1haWwiOiJqb3JnZXF1aWd1YW5nb0BvdXRsb29rLmNvbSIsInJvbGUiOiJNYW5hZ2VyIiwic2VydmljZSI6IlJlSWRpIiwibGltaXQiOjB9.X_g2Et9T3P_ZyCZcxB_esNfTlF7PzBYFIYTFSAJgeIo', 
9, 0);
