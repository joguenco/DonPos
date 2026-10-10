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

CREATE or REPLACE FUNCTION `fun_tip`(`p_ticket` varchar(90)) RETURNS decimal(19,2) BEGIN DECLARE `v_tip` decimal(19, 2); SELECT nvl(sum(`units` * `price` ), 0) into `v_tip` from `ticketlines` where `ticket` = `p_ticket` and `product` = 'xxx998_998xxx_x8x8x8'; return v_tip; END;

delete from taxpayer where identification = '000000000';

-- text_1 -> forced_accounting
-- text_2 -> special_taxpayer
-- text_3 -> retention_agent
-- text_4 -> regime
INSERT INTO taxpayer (id, identification, legal_name, text_1, text_2, text_3, text_4)
VALUES (1, '9999999999999', 'Mi Empresa', 'SI', '12345', '1', 'CONTRIBUYENTE RÉGIMEN RIMPE');

-- Customer default
delete from resources where id = '90';
INSERT INTO resources(id, name, restype, content) VALUES('90', 'Customer.Default', 0, $FILE{/com/unicenta/pos/templates/Customer.Default.EC.txt});
-- TABLES
CREATE TABLE `ticketsnum_purchase` (
    `code`      varchar(10) not null,
    `people_id` varchar(255) not null,
    `serie`     varchar(100) not null,
    `id`        int(11) NOT NULL,    
    `priority`  varchar(20) not null,
    `status`    varchar(10) not null,
    primary key (`code`, `people_id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 ;

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
    `authorization_code` VARCHAR(90) DEFAULT NULL,
    `authorization_date` DATETIME DEFAULT NULL,
    `observation` VARCHAR(5400) DEFAULT NULL,
    `status` VARCHAR(30) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ele` (`code` , `number`) USING BTREE,
    KEY `inx_fecha` (`code` , `number` , `status`) USING BTREE
)  ENGINE = InnoDB DEFAULT CHARSET=utf8;

-- Catálogo de tipos de retención del SRI
CREATE TABLE `withhold_taxes` (
    `id` varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    `percentage` double NOT NULL DEFAULT '0',
    `code` varchar(90) DEFAULT NULL,
    `tax_type` varchar(90) NOT NULL,
    `created_at` date DEFAULT NULL,
    `status` boolean DEFAULT true,
    PRIMARY KEY (`id`),
    KEY `withhold_taxes_code_inx` (`code`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

-- Cabecera del comprobante de retención
CREATE TABLE `withholds` (
    `id` varchar(255) NOT NULL,
    `code` varchar(10) NOT NULL DEFAULT 'RT',
    `serie_number` varchar(100) NOT NULL,
    `purchase_id` varchar(255) NOT NULL,
    `date_withhold` datetime NOT NULL,
    `observation` varchar(900) DEFAULT NULL,
    `fiscal_period` date DEFAULT NULL,
    `access_key` varchar(180) DEFAULT NULL,
    `status` boolean DEFAULT true,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_withholds` (`code`, `serie_number`) USING BTREE,
    KEY `withholds_purchase_inx` (`purchase_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

-- Detalle: las retenciones aplicadas
    -- Sustento tributario de esta linea. El SRI agrupa las retenciones por
    -- sustento: cada docSustento del XML lleva uno solo y sus retenciones
    -- adentro, asi que la linea tiene que saber a cual pertenece.
CREATE TABLE `withholds_detail` (
    `withhold_id` varchar(255) NOT NULL,
    `line` int(11) NOT NULL,
    `tax_support` varchar(90) default NULL,
    `withhold_taxes_id` varchar(255) NOT NULL,
    `percentage` double NOT NULL DEFAULT '0',
    `base_value` double NOT NULL DEFAULT '0',
    `withholded_value` double NOT NULL DEFAULT '0',
    `tax_rate` double DEFAULT '-1',
    PRIMARY KEY (`withhold_id`, `line`),
    KEY `withholds_detail_taxes_inx` (`withhold_taxes_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

-- RELATIONS
alter table `ticketsnum_purchase` add constraint `ticketsnum_purchase_people_fk`
    foreign key ( `people_id` ) references `people` ( `id` );

alter table `withholds` add constraint `withholds_purchase_fk`
    foreign key ( `purchase_id` ) references `purchases` ( `id` );

alter table `withholds_detail` add constraint `withholds_detail_fk`
    foreign key ( `withhold_id` ) references `withholds` ( `id` ) on delete cascade;

alter table `withholds_detail` add constraint `withholds_detail_taxes_fk`
    foreign key ( `withhold_taxes_id` ) references `withhold_taxes` ( `id` );

-- Una sola retencion por compra: el comprobante de retencion se emite sobre un
-- unico documento de sustento. El SRI admite varios (docSustento es una lista),
-- pero el modelo de DonPos es 1:1 y la base lo garantiza.
alter table `withholds` add unique index `uk_withholds_purchase` ( `purchase_id` );

-- VIEWs 
CREATE VIEW `v_ele_taxpayer` AS SELECT 
        `id`,
        `identification`,
        `legal_name`,
        `text_1` as `forced_accounting`,
        `text_2` as `special_taxpayer`,
        `text_3` as `retention_agent`,
        `text_4` as `regime`
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
    (cast(`t`.`id` as uuid)) as `id`,
    `t`.`code` AS `code`,
    `t`.`serie_number` AS `number`,
    cast('01' as char) AS `code_document`,
    substr(`t`.`serie_number`, 1, 3) AS `establishment`,
    substr(`t`.`serie_number`, 4, 3) AS `emission_point`,
    cast(lpad(`t`.`ticketid`, 9, '0') as char) AS `sequence`,
    cast(`r`.`datenew` as date) AS `date`,
    round(sum(cast(`tl`.`units` * `tl`.`price` as decimal(19, 2))), 2) as `total_without_taxes`,
    round(sum(cast((((100 * `tl`.`price` / (100 - `tl`.`discount`)) * (`tl`.`discount` / 100)) * `tl`.`units`) AS DECIMAL (19 , 2 ))), 2) `discount`,
    round(sum(cast(`tl`.`units` * `tl`.`price` + if(`tx`.`rate` > 0, `tl`.`units` * `tl`.`price` * `tx`.`rate`, 0) as decimal(19, 2))) + `fun_tip`(`t`.`id`), 2) AS `total`,
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
    `t`.`access_key` as `access_key`,
    `fun_tip`(`t`.`id`) AS `tip`
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
    and (`tl`.`product` <> 'xxx998_998xxx_x8x8x8')
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
    (cast(concat(SUBSTRING(`tl`.`ticket`, 1, LENGTH(`tl`.`ticket`) - LENGTH(`tl`.`line`)), `tl`.`line`) as uuid)) AS `id`,
    t.code,
    `t`.`serie_number` AS `number`,
    `p`.`REFERENCE` AS `principal_code`,
    CAST(`tl`.`line` AS UNSIGNED) AS `line`,
    `p`.`name`,
    CAST(`tl`.`units` AS DECIMAL (19 , 2 )) AS `quantity`,
    CONVERT('UN', CHAR) AS `unit`,
    CAST((100 * `tl`.`price` / (100 - `tl`.`discount`)) as DECIMAL(19, 2)) AS `unit_price`,
    CONVERT(`tx`.`legalcode`, CHAR) AS `tax_code`,
    CAST((`tx`.`rate` * 100) AS DECIMAL (19 , 2 )) AS `tax_iva`,
    CAST(((`tl`.`units` * `tl`.`price`) * `tx`.`rate`)
        AS DECIMAL (19 , 2 )) AS `value_iva`,
    CAST((((100 * `tl`.`price` / (100 - `tl`.`discount`)) * (`tl`.`discount` / 100)) * `tl`.`units`) AS DECIMAL (19 , 2 )) AS `discount`,
    CAST((`tl`.`units` * `tl`.`price`) AS DECIMAL (19 , 2 )) AS `total_price_without_tax`
FROM
    (((`tickets` `t`
    JOIN `ticketlines` `tl` ON ((`t`.`ID` = `tl`.`TICKET`)))
    JOIN `taxes` `tx` ON ((`tx`.`CATEGORY` = `tl`.`TAXID`)))
    JOIN `products` `p` ON ((`p`.`ID` = `tl`.`PRODUCT`)))
WHERE
    (`t`.`TICKETTYPE` = 0)
and (`tl`.`product` <> 'xxx998_998xxx_x8x8x8');

CREATE VIEW `v_ele_credit_notes` as SELECT
    (cast(`t`.`id` as uuid)) as `id`,
    `t`.`code` AS `code`,
    `t`.`serie_number` AS `number`,
    cast('04' as char) AS `code_document`,
    substr(`t`.`serie_number`, 1, 3) AS `establishment`,
    substr(`t`.`serie_number`, 4, 3) AS `emission_point`,
    cast(lpad(`t`.`ticketid`, 9, '0') as char) AS `sequence`,
    cast(`r`.`datenew` as date) AS `date`,
    cast('01' as char) AS `updated_code_document`,
    (
    select
            `ut`.`serie_number`
    from
            `tickets` `ut`
    where
            `ut`.`id` = `t`.`tickets_id`) AS `updated_number_document`,
    (
    select
            (cast(`ur`.`datenew` as date))
    from
            `receipts` `ur`
    where
            `ur`.`id` = `t`.`tickets_id`) AS `updated_date_document`,
    abs(round(sum(cast(`tl`.`units` * `tl`.`price` as decimal(19, 2))), 2)) AS `total_without_taxes`,
    abs(round(sum(cast(`tl`.`units` * `tl`.`price` + if(`tx`.`rate` > 0, `tl`.`units` * `tl`.`price` * `tx`.`rate`, 0) as decimal(19, 2))), 2)) AS `total`,
    `i`.`legal_code` AS `identification_type`,
    `c`.`taxid` AS `identification`,
    `c`.`name` AS `legal_name`,
    `c`.`address` AS `address`,
    cast('Devolución' as char(20)) AS `reason`,
    (
    select
            `e`.`address`
    from
            `establishments` `e`
    where
            `e`.`id` = substr(`t`.`serie_number`, 1, 3)) AS `establishment_address`,
    `t`.`access_key` AS `access_key`
from
	(((((`tickets` `t`
join `receipts` `r` on
	(`t`.`id` = `r`.`id`))
join `customers` `c` on
	(`c`.`id` = `t`.`customer`))
join `identification_type` `i` on
	(`i`.`code` = `c`.`taxid_type`))
join `ticketlines` `tl` on
	(`t`.`id` = `tl`.`ticket`))
join `taxes` `tx` on
	(`tx`.`category` = `tl`.`taxid`))
where
	(`t`.`code` = 'DV')
	and (`t`.`tickettype` = 1)
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
	`c`.`address`;

CREATE VIEW `v_ele_credit_notes_detail` as select
    (cast(concat(substr(`tl`.`ticket`, 1, octet_length(`tl`.`ticket`) - octet_length(`tl`.`line`)), `tl`.`line`) as uuid)) AS `id`,
    `t`.`code` as `code`,
    `t`.`serie_number` as `number`,
    `p`.`reference` as `principal_code`,
    cast(`tl`.`line` as unsigned) as `line`,
    `p`.`name` as `name`,
    cast(abs(`tl`.`units`) as decimal(19, 2)) as `quantity`,
    cast(`tl`.`price` as decimal(19, 2)) as `unit_price`,
    cast(`tx`.`legalcode` as char ) AS `tax_code`,
    cast(`tx`.`rate` * 100 as decimal(19, 2)) as `tax_iva`,
    cast(abs(`tl`.`units` * `tl`.`price` * `tx`.`rate`) as decimal(19, 2)) as `value_iva`,    
    cast(0 as decimal(19, 2)) as `discount`,
    cast(abs(`tl`.`units` * `tl`.`price`) as decimal(19, 2)) as `total_price_without_tax`           
from
    (((`tickets` `t`
join `ticketlines` `tl` on
    (`t`.`id` = `tl`.`ticket`))
join `taxes` `tx` on
    (`tx`.`category` = `tl`.`taxid`))
join `products` `p` on
    (`p`.`id` = `tl`.`product`))
where
    (`t`.`tickettype` = 1);

CREATE VIEW `v_ele_taxes_detail` as SELECT 
        (cast(concat(SUBSTRING(`tl`.`ticket`, 1, (LENGTH(`tl`.`ticket`) - LENGTH(`tl`.`line`)) - 1), `tl`.`line`, '2') as uuid)) AS `id`,
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
        JOIN `products` `p` ON ((`p`.`ID` = `tl`.`PRODUCT`)))
    where (`tl`.`product` <> 'xxx998_998xxx_x8x8x8');

CREATE VIEW v_ele_information as WITH information AS 
   (SELECT 1 AS `id`,`TAXID` AS `identification`,
	'Email' AS `name`,
	`EMAIL` AS `value`
FROM `customers`
WHERE
	((`EMAIL` IS NOT NULL)
		AND (`EMAIL` <> ''))
UNION all SELECT 2 AS `id`,
	`TAXID`,
	'Dirección',
	`ADDRESS`
FROM
	`customers`
WHERE
	((`ADDRESS` IS NOT NULL)
		AND (`ADDRESS` <> ''))
UNION all SELECT 3 AS `id`,
	`TAXID`,
	'Teléfono',
	`PHONE`
FROM
	`customers`
WHERE
	((`PHONE` IS NOT NULL)
		AND (`PHONE` <> ''))
		)
SELECT `information`.`id` AS `id`,
	`identification`,
	`name`,
	`value` FROM information;

CREATE  VIEW `v_ele_payments` AS SELECT cast(`p`.`id` as uuid) AS `id`,
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
    `j`.`access_key` AS `access_key`,
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

CREATE  VIEW `v_ele_report_credit_notes` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`access_key` AS `access_key`,
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
    `v_ele_credit_notes` `j`
order by `j`.`number` desc, `j`.`number` desc;

CREATE VIEW `v_ele_debit_notes` AS select cast(`t`.`id` as uuid) AS `id`,
    `t`.`code` AS `code`,
    `t`.`serie_number` AS `number`,
    cast('05' as char) AS `code_document`,
    substr(`t`.`serie_number`, 1, 3) AS `establishment`,
    substr(`t`.`serie_number`, 4, 3) AS `emission_point`,
    cast(lpad(`t`.`ticketid`, 9, '0') as char) AS `sequence`,
    cast(`r`.`datenew` as date) AS `date`,
    cast('01' as char) AS `updated_code_document`,
    (
    select
            `ut`.`serie_number`
    from
            `tickets` `ut`
    where
            `ut`.`id` = `t`.`tickets_id`) AS `updated_number_document`,
    (
    select
            (cast(`ur`.`datenew` as date))
    from
            `receipts` `ur`
    where
            `ur`.`id` = `t`.`tickets_id`) AS `updated_date_document`,
    abs(round(sum(cast(`tl`.`units` * `tl`.`price` as decimal(19,2))), 2)) AS `total_without_taxes`,
    abs(round(sum(cast(`tl`.`units` * `tl`.`price` + if(`tx`.`rate` > 0, `tl`.`units` * `tl`.`price` * `tx`.`rate`, 0) as decimal(19,2))), 2)) AS `total`,
    `i`.`legal_code` AS `identification_type`,
    `c`.`taxid` AS `identification`,
    `c`.`name` AS `legal_name`,
    `c`.`address` AS `address`,
    (select `e`.`address` from `establishments` `e` where `e`.`id` = substr(`t`.`serie_number`, 1, 3)) AS `establishment_address`,
    `t`.`access_key` AS `access_key`
from (((((`tickets` `t`
    join `receipts` `r` on(`t`.`id` = `r`.`id`))
    join `customers` `c` on(`c`.`id` = `t`.`customer`))
    join `identification_type` `i` on(`i`.`code` = `c`.`taxid_type`))
    join `ticketlines` `tl` on(`t`.`id` = `tl`.`ticket`))
    join `taxes` `tx` on(`tx`.`category` = `tl`.`taxid`))
where `t`.`code` = 'ND'
group by `t`.`id`, `t`.`code`, `t`.`serie_number`, `t`.`ticketid`, `t`.`tickets_id`,
    `t`.`access_key`, `r`.`datenew`, `i`.`legal_code`, `c`.`taxid`, `c`.`name`, `c`.`address`;

CREATE VIEW `v_ele_debit_notes_detail` AS select cast(concat(substr(`tl`.`ticket`, 1, octet_length(`tl`.`ticket`) - octet_length(`tl`.`line`)), `tl`.`line`) as uuid) AS `id`,
    `t`.`code` AS `code`,
    `t`.`serie_number` AS `number`,
    cast(`tl`.`line` as unsigned) AS `line`,
    `p`.`name` AS `reason`,
    cast(abs(`tl`.`units` * `tl`.`price`) as decimal(19,2)) AS `value`
from ((`tickets` `t`
    join `ticketlines` `tl` on(`t`.`id` = `tl`.`ticket`))
    join `products` `p` on(`p`.`id` = `tl`.`product`))
where `t`.`code` = 'ND';

CREATE VIEW `v_ele_report_debit_notes` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`access_key` AS `access_key`,
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
    `v_ele_debit_notes` `j`
order by `j`.`number` desc;

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

CREATE VIEW `v_assets` AS select `id` AS `id`,
    `name` AS `name`,
    convert(`content` using utf8mb3) AS `value`
from `resources`
where `name` = 'Electronic.Environment';

CREATE VIEW `v_ele_general_informations` AS select `p`.`id` AS `id`,
    `p`.`name`  AS `name`,
    `p`.`value` AS `value`
from `ele_parameters` `p`
where `p`.`name` = 'RUC Proveedor'
and `p`.`status` = true;

-- Vistas de liquidación de compra
CREATE VIEW `v_ele_liquidations` AS select cast(`p`.`id` as uuid) AS `id`,
    cast('LQ' as char) AS `code`,
    `p`.`purchase_reference` AS `number`,
    cast('03' as char) AS `code_document`,
    substr(`p`.`purchase_reference`, 1, 3) AS `establishment`,
    substr(`p`.`purchase_reference`, 4, 3) AS `emission_point`,
    substr(`p`.`purchase_reference`, 7, 9) AS `sequence`,
    cast(`p`.`purchase_date` as date) AS `date`,
    round(sum(cast(`pl`.`units` * `pl`.`price` as decimal(19, 2))), 2) AS `total_without_taxes`,
    cast(0 as decimal(19, 2)) AS `discount`,
    round(sum(cast(`pl`.`units` * `pl`.`price` + if(`tx`.`rate` > 0, `pl`.`units` * `pl`.`price` * `tx`.`rate`, 0) as decimal(19, 2))), 2) AS `total`,
    `i`.`legal_code` AS `identification_type`,
    `s`.`taxid` AS `identification`,
    `s`.`name` AS `legal_name`,
    `s`.`address` AS `address`,
    (select
        `e`.`address`
    from
        `establishments` `e`
    where
        `e`.`id` = substr(`p`.`purchase_reference`, 1, 3)) AS `establishment_address`,
    `p`.`purchase_authorization` AS `access_key`
from
    (`purchases` `p`
join `suppliers` `s` on
    `s`.`id` = `p`.`supplier`
join `identification_type` `i` on
    `i`.`code` = `s`.`taxid_type`
join `purchaselines` `pl` on
    `pl`.`purchase` = `p`.`id`
join `taxes` `tx` on
    `tx`.`id` = `pl`.`taxid`)
where
    (`p`.`purchase_document` = '03')
    and (`p`.`status` = 1)
group by
    `p`.`id`,
    `p`.`purchase_reference`,
    `p`.`purchase_date`,
    `i`.`legal_code`,
    `s`.`taxid`,
    `s`.`name`,
    `s`.`address`,
    `p`.`purchase_authorization`;

CREATE VIEW `v_ele_liquidations_detail` AS select cast(concat(substr(`pl`.`purchase`, 1, octet_length(`pl`.`purchase`) - octet_length(`pl`.`line`)), `pl`.`line`) as uuid) AS `id`,
    cast('LQ' as char) AS `code`,
    `p`.`purchase_reference` AS `number`,
    `pr`.`reference` AS `principal_code`,
    cast(`pl`.`line` as unsigned) AS `line`,
    `pr`.`name` AS `name`,
    cast(`pl`.`units` as decimal(19, 2)) AS `quantity`,
    convert('UN', char) AS `unit`,
    cast(`pl`.`price` as decimal(19, 2)) AS `unit_price`,
    convert(`tx`.`legalcode`, char) AS `tax_code`,
    cast(`tx`.`rate` * 100 as decimal(19, 2)) AS `tax_iva`,
    cast(`pl`.`units` * `pl`.`price` * `tx`.`rate` as decimal(19, 2)) AS `value_iva`,
    cast(0 as decimal(19, 2)) AS `discount`,
    cast(`pl`.`units` * `pl`.`price` as decimal(19, 2)) AS `total_price_without_tax`
from
    (`purchases` `p`
join `purchaselines` `pl` on
    `pl`.`purchase` = `p`.`id`
join `products` `pr` on
    `pr`.`id` = `pl`.`product`
join `taxes` `tx` on
    `tx`.`id` = `pl`.`taxid`)
where
    (`p`.`purchase_document` = '03')
    and (`p`.`status` = 1);

CREATE VIEW `v_ele_liquidations_taxes` AS select cast(concat(substr(`pl`.`purchase`, 1, octet_length(`pl`.`purchase`) - octet_length(`pl`.`line`) - 1), `pl`.`line`, '2') as uuid) AS `id`,
    cast('LQ' as char) AS `code`,
    `p`.`purchase_reference` AS `number`,
    `pr`.`reference` AS `principal_code`,
    cast(`pl`.`line` as unsigned) AS `line`,
    convert('2', char) AS `tax_code`,
    convert(`tx`.`legalcode`, char) AS `percentage_code`,
    abs(cast(`pl`.`units` * `pl`.`price` as decimal(19, 2))) AS `tax_base`,
    cast(`tx`.`rate` * 100 as decimal(19, 2)) AS `tax_iva`,
    abs(cast(`pl`.`units` * `pl`.`price` * `tx`.`rate` as decimal(19, 2))) AS `value`
from
    (`purchases` `p`
join `purchaselines` `pl` on
    `pl`.`purchase` = `p`.`id`
join `products` `pr` on
    `pr`.`id` = `pl`.`product`
join `taxes` `tx` on
    `tx`.`id` = `pl`.`taxid`)
where
    (`p`.`purchase_document` = '03')
    and (`p`.`status` = 1);

CREATE VIEW `v_ele_report_liquidations` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`access_key` AS `access_key`,
    `j`.`date` AS `date`,
    `j`.`total` AS `total`,
    `j`.`identification` AS `identification`,
    `j`.`legal_name` AS `legal_name`,
    (select `sup`.`email` from `suppliers` `sup` where `sup`.`taxid` = `j`.`identification` limit 1) AS `email`,
    ifnull((select `e`.`status` from `ele_documents` `e` where `e`.`code` = `j`.`code` and `e`.`number` = `j`.`number`), 'NO ENVIADO') AS `status`
from `v_ele_liquidations` `j`
order by `j`.`number` desc;

-- Vistas de retención
CREATE VIEW `v_ele_withholds` AS select cast(`w`.`id` as uuid) AS `id`,
    `w`.`code` AS `code`,
    `w`.`serie_number` AS `number`,
    cast('07' as char) AS `code_document`,
    substr(`w`.`serie_number`, 1, 3) AS `establishment`,
    substr(`w`.`serie_number`, 4, 3) AS `emission_point`,
    substr(`w`.`serie_number`, 7, 9) AS `sequence`,
    cast(`w`.`date_withhold` as date) AS `date`,
    date_format(`w`.`fiscal_period`, '%m/%Y') AS `fiscal_period`,
    `i`.`legal_code` AS `identification_type`,
    `s`.`taxid` AS `identification`,
    `s`.`name` AS `legal_name`,
    if(`s`.`is_related` = true, 'SI', 'NO') AS `related`,
    (select
        `e`.`address`
    from
        `establishments` `e`
    where
        `e`.`id` = substr(`w`.`serie_number`, 1, 3)) AS `establishment_address`,
    `w`.`access_key` AS `access_key`
from
    (`withholds` `w`
join `purchases` `p` on
    `p`.`id` = `w`.`purchase_id`
join `suppliers` `s` on
    `s`.`id` = `p`.`supplier`
join `identification_type` `i` on
    `i`.`code` = `s`.`taxid_type`
join `purchaselines` `pl` on
    `pl`.`purchase` = `p`.`id`
join `taxes` `tx` on
    `tx`.`id` = `pl`.`taxid`)
where
    (`w`.`status` = 1)
group by
    `w`.`id`,
    `w`.`code`,
    `w`.`serie_number`,
    `w`.`date_withhold`,
    `w`.`fiscal_period`,
    `i`.`legal_code`,
    `s`.`taxid`,
    `s`.`name`,
    `s`.`address`,
    `p`.`purchase_tax_support`,
    `p`.`purchase_document`,
    `p`.`purchase_reference`,
    `p`.`purchase_date`,
    `p`.`purchase_authorization`,
    `w`.`access_key`;

-- Impuestos del documento de sustento, separados POR SUSTENTO.
-- Antes se sumaban para toda la compra, y con dos bloques docSustento cada uno
-- se llevaba el total completo: el IVA quedaba contado dos veces. Odoo hace lo
-- mismo que aqui, filtra las lineas de la factura por sustento antes de sumar.
CREATE VIEW `v_ele_withholds_document_taxes` AS select cast(concat(substr(`w`.`id`, 1, octet_length(`w`.`id`) - octet_length(concat(`tx`.`legalcode`, ifnull(`pl`.`tax_support`, `p`.`purchase_tax_support`)))), `tx`.`legalcode`, ifnull(`pl`.`tax_support`, `p`.`purchase_tax_support`)) as uuid) AS `id`,
    `w`.`code` AS `code`,
    `w`.`serie_number` AS `number`,
    ifnull(`pl`.`tax_support`, `p`.`purchase_tax_support`) AS `code_support`,
    convert('2', char) AS `tax_code`,
    convert(`tx`.`legalcode`, char) AS `percentage_code`,
    round(sum(cast(`pl`.`units` * `pl`.`price` as decimal(19, 2))), 2) AS `tax_base`,
    cast(`tx`.`rate` * 100 as decimal(19, 2)) AS `tax_iva`,
    round(sum(cast(`pl`.`units` * `pl`.`price` * `tx`.`rate` as decimal(19, 2))), 2) AS `value`
from
    (`withholds` `w`
join `purchases` `p` on
    `p`.`id` = `w`.`purchase_id`
join `purchaselines` `pl` on
    `pl`.`purchase` = `p`.`id`
join `taxes` `tx` on
    `tx`.`id` = `pl`.`taxid`)
where
    (`w`.`status` = 1)
group by
    `w`.`id`,
    `w`.`code`,
    `w`.`serie_number`,
    ifnull(`pl`.`tax_support`, `p`.`purchase_tax_support`),
    `tx`.`legalcode`,
    `tx`.`rate`;

CREATE VIEW `v_ele_withholds_detail` AS select cast(concat(substr(`d`.`withhold_id`, 1, octet_length(`d`.`withhold_id`) - octet_length(`d`.`line`)), `d`.`line`) as uuid) AS `id`,
    `w`.`code` AS `code`,
    `w`.`serie_number` AS `number`,
    cast(`d`.`line` as unsigned) AS `line`,
    (case `t`.`tax_type` when 'RENTA' then '1' when 'IVA' then '2' else '6' end) AS `tax_code`,
    `t`.`code` AS `withhold_code`,
    cast(`d`.`base_value` as decimal(19, 2)) AS `base_value`,
    cast(`d`.`percentage` as decimal(19, 2)) AS `percentage`,
    cast(`d`.`withholded_value` as decimal(19, 2)) AS `withholded_value`,
    ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`) AS `code_support`
from
    (((`withholds_detail` `d`
join `withholds` `w` on
    (`w`.`id` = `d`.`withhold_id`))
join `purchases` `p` on
    (`p`.`id` = `w`.`purchase_id`))
join `withhold_taxes` `t` on
    (`t`.`id` = `d`.`withhold_taxes_id`));

-- Un bloque docSustento por cada sustento QUE SE RETUVO.
-- Se parte de withholds_detail, no de purchaselines: el XSD exige al menos una
-- retencion dentro de cada bloque, asi que un sustento que la compra tiene pero
-- sobre el que no se retuvo dejaria el bloque vacio y el XML invalido. Odoo hace
-- lo mismo: recorre las lineas de la retencion, no las de la factura.
--
-- El ifnull cubre las retenciones viejas, guardadas antes de que DonPos pidiera
-- el sustento por linea: caen al sustento de la cabecera de la compra.
CREATE VIEW `v_ele_withholds_support` AS select cast(concat(substr(`w`.`id`, 1, octet_length(`w`.`id`) - octet_length(ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`))), ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`)) as uuid) AS `id`,
    `w`.`code` AS `code`,
    `w`.`serie_number` AS `number`,
    ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`) AS `code_support`,
    `p`.`purchase_document` AS `code_document_support`,
    if(length(`p`.`purchase_reference`) = 15,
        concat(substr(`p`.`purchase_reference`, 1, 3), '-',
               substr(`p`.`purchase_reference`, 4, 3), '-',
               substr(`p`.`purchase_reference`, 7, 9)),
        `p`.`purchase_reference`) AS `number_document_support`,
    cast(`p`.`purchase_date` as date) AS `date_document_support`,
    `p`.`purchase_authorization` AS `authorization_document_support`,
    round(sum(cast(`l`.`units` * `l`.`price` as decimal(19, 2))), 2) AS `total_without_taxes`,
    round(sum(cast(`l`.`units` * `l`.`price`
        + if(`tx`.`rate` > 0, `l`.`units` * `l`.`price` * `tx`.`rate`, 0)
        as decimal(19, 2))), 2) AS `total`
from
    ((((`withholds` `w`
join `purchases` `p` on
    (`p`.`id` = `w`.`purchase_id`))
join (select distinct `withhold_id`, `tax_support` from `withholds_detail`) `d` on
    (`d`.`withhold_id` = `w`.`id`))
join `purchaselines` `l` on
    (`l`.`purchase` = `p`.`id`
        and ifnull(`l`.`tax_support`, `p`.`purchase_tax_support`)
            = ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`)))
join `taxes` `tx` on
    (`tx`.`id` = `l`.`taxid`))
where
    (`w`.`status` = 1)
group by
    `w`.`id`,
    `w`.`code`,
    `w`.`serie_number`,
    ifnull(`d`.`tax_support`, `p`.`purchase_tax_support`),
    `p`.`purchase_document`,
    `p`.`purchase_reference`,
    `p`.`purchase_date`,
    `p`.`purchase_authorization`;

CREATE VIEW `v_ele_report_withholds` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`access_key` AS `access_key`,
    `j`.`date` AS `date`,
    ifnull((select sum(`d`.`withholded_value`) from `v_ele_withholds_detail` `d` where `d`.`code` = `j`.`code` and `d`.`number` = `j`.`number`), 0) AS `total`,
    `j`.`identification` AS `identification`,
    `j`.`legal_name` AS `legal_name`,
    (select `sup`.`email` from `suppliers` `sup` where `sup`.`taxid` = `j`.`identification` limit 1) AS `email`,
    ifnull((select `e`.`status` from `ele_documents` `e` where `e`.`code` = `j`.`code` and `e`.`number` = `j`.`number`), 'NO ENVIADO') AS `status`
from `v_ele_withholds` `j`
order by `j`.`number` desc;

-- Vistas de guía de remisión
-- El SRI arma el documento en tres niveles: la cabecera con el transportista,
-- un destinatario por cada factura del viaje, y el detalle de mercadería de
-- cada destinatario. Las tres vistas siguen esa forma.

CREATE VIEW `v_ele_delivery_notes` AS select cast(`d`.`id` as uuid) AS `id`,
    `d`.`code` AS `code`,
    `d`.`serie_number` AS `number`,
    cast('06' as char) AS `code_document`,
    substr(`d`.`serie_number`, 1, 3) AS `establishment`,
    substr(`d`.`serie_number`, 4, 3) AS `emission_point`,
    substr(`d`.`serie_number`, 7, 9) AS `sequence`,
    cast(`d`.`date_dispatch` as date) AS `date`,
    `d`.`address_start` AS `address_start`,
    `p`.`name` AS `carrier_legal_name`,
    `i`.`legal_code` AS `carrier_identification_type`,
    `p`.`taxid` AS `carrier_identification`,
    `p`.`plate` AS `plate`,
    cast(`d`.`date_dispatch` as date) AS `date_start_transport`,
    cast(`d`.`date_end_dispatch` as date) AS `date_end_transport`,
    `d`.`observation` AS `observation`,
    (select
        `e`.`address`
    from
        `establishments` `e`
    where
        `e`.`id` = substr(`d`.`serie_number`, 1, 3)) AS `establishment_address`,
    `d`.`access_key` AS `access_key`
from
    ((`dispatches` `d`
join `dispatchers` `p` on
    (`p`.`id` = `d`.`dispatcher_id`))
join `identification_type` `i` on
    (`i`.`code` = `p`.`taxid_type`))
where
    (`d`.`status` = 1);

-- Un destinatario por factura. El documento de sustento es siempre la factura
-- de venta (codDoc 01): de ahi salen el cliente, la direccion de entrega y la
-- autorizacion que el SRI pide en numAutDocSustento.
CREATE VIEW `v_ele_delivery_notes_receiver` AS select cast(concat(substr(`dd`.`dispatches_id`, 1, octet_length(`dd`.`dispatches_id`) - octet_length(`dd`.`line`)), `dd`.`line`) as uuid) AS `id`,
    `d`.`code` AS `code`,
    `d`.`serie_number` AS `number`,
    cast(`dd`.`line` as unsigned) AS `line`,
    `i`.`legal_code` AS `identification_type`,
    `c`.`taxid` AS `identification`,
    `c`.`name` AS `legal_name`,
    `c`.`address` AS `address`,
    `dd`.`transfer_reason` AS `transfer_reason`,
    cast('01' as char) AS `code_document_support`,
    if(length(`dd`.`reference_number`) = 15,
        concat(substr(`dd`.`reference_number`, 1, 3), '-',
               substr(`dd`.`reference_number`, 4, 3), '-',
               substr(`dd`.`reference_number`, 7, 9)),
        `dd`.`reference_number`) AS `number_document_support`,
    `t`.`access_key` AS `authorization_document_support`,
    cast(`r`.`datenew` as date) AS `date_document_support`
from
    (((((`dispatches_detail` `dd`
join `dispatches` `d` on
    (`d`.`id` = `dd`.`dispatches_id`))
join `tickets` `t` on
    (`t`.`code` = `dd`.`reference_code`
        and `t`.`serie_number` = `dd`.`reference_number`))
join `receipts` `r` on
    (`r`.`id` = `t`.`id`))
join `customers` `c` on
    (`c`.`id` = `t`.`customer`))
join `identification_type` `i` on
    (`i`.`code` = `c`.`taxid_type`))
where
    (`d`.`status` = 1);

-- La mercaderia que va en el camion, sacada de las lineas de la factura. El
-- SRI pide descripcion y cantidad por cada destinatario; el campo `line` dice
-- a que destinatario pertenece cada producto.
CREATE VIEW `v_ele_delivery_notes_receiver_detail` AS select cast(concat(substr(`tl`.`ticket`, 1, octet_length(`tl`.`ticket`) - octet_length(`tl`.`line`)), `tl`.`line`) as uuid) AS `id`,
    `d`.`code` AS `code`,
    `d`.`serie_number` AS `number`,
    cast(`dd`.`line` as unsigned) AS `line`,
    `pr`.`reference` AS `principal_code`,
    `pr`.`name` AS `name`,
    cast(`tl`.`units` as decimal(19, 2)) AS `quantity`
from
    ((((`dispatches_detail` `dd`
join `dispatches` `d` on
    (`d`.`id` = `dd`.`dispatches_id`))
join `tickets` `t` on
    (`t`.`code` = `dd`.`reference_code`
        and `t`.`serie_number` = `dd`.`reference_number`))
join `ticketlines` `tl` on
    (`tl`.`ticket` = `t`.`id`))
join `products` `pr` on
    (`pr`.`id` = `tl`.`product`))
where
    (`d`.`status` = 1)
    and (`tl`.`product` <> 'xxx998_998xxx_x8x8x8');

CREATE VIEW `v_ele_report_delivery_notes` AS select `j`.`id` AS `id`,
    `j`.`code` AS `code`,
    `j`.`number` AS `number`,
    `j`.`access_key` AS `access_key`,
    `j`.`date` AS `date`,
    `j`.`carrier_identification` AS `identification`,
    `j`.`carrier_legal_name` AS `legal_name`,
    `j`.`plate` AS `plate`,
    ifnull((select `e`.`status` from `ele_documents` `e`
        where `e`.`code` = `j`.`code` and `e`.`number` = `j`.`number`), 'NO ENVIADO') AS `status`
from `v_ele_delivery_notes` `j`
order by `j`.`number` desc;

Insert into ele_parameters (ID,name,value,observation,type) 
values (1,'Base Directory','/app/RoQui','Base directory for files','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (2,'Certificate','Certificate.p12','Certificate name','Certificate');
Insert into ele_parameters (ID,name,value,observation,type) 
values (3,'Certificate Password','***************==','Certificate Password','Certificate');
Insert into ele_parameters (ID,name,value,observation,type) 
values (4,'Logo JPEG','logo.jpeg','URL logo JPEG','SRI');
Insert into ele_parameters (ID,name,value,observation,type) 
values (5,'Email SMTP Server','localhost','Email SMTP Server','Email SMTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (6,'Port Email SMTP Server','1025','Port Email SMTP Server','Email SMTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (7,'Email Account','hola@localhost','Account of Email SMTP Server','Email SMTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (8,'Email Password Account','','Password Account of Email SMTP Server','Email SMTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (9,'Email Encryption','None','Connection Encryption: None, SSL/TLS ','Email SMTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (10,'Email HTTP Server','https://mail.server.com','Email HTTP Server','Email HTTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (11,'Email HTTP Server Token','**************==','Token Email HTTP Server','Email HTTP');
Insert into ele_parameters (ID,name,value,observation,type) 
values (12,'Logo PNG','logo.png','URL Logo PNG','Resource');
Insert into ele_parameters (ID,name,value,observation,type)
values (13,'Template Email','template.html','URL template','Resource');
Insert into ele_parameters (ID,name,value,observation,type)
values (14, 'RoQui HTTP Server', 'http://localhost:8080', 'Server for electronic documents authorization', 'Resource');
Insert into ele_parameters (ID,name,value,observation,type)
values (15, 'RoQui HTTP X-API-KEY', 'api__6tpXYCxsXpCs7QeuI44KtoCq', 'X-API-KEY for authentication', 'Resource');
Insert into ele_parameters (ID,name,value,observation,type)
values (16, 'RUC Proveedor', '0123456789001', 'Identification of the provider', 'SRI' );

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

-- ADD UOM
INSERT INTO uom(id, name) VALUES ('u','Unidad');

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

INSERT INTO services (id,name,url,authentication_method,token,timeout,status) 
VALUES ('1', 'ReIdi', 'https://reidi.ec.service.joguenco.dev', 
'Token',
'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJyZWlkaS5zZXJ2aWNlLmpvZ3VlbmNvLmRldiIsImlhdCI6MTc0MDM2NjM2OSwiZXhwIjoxNzU1OTE4MzY5LCJhdWQiOiJqb2d1ZW5jby5kZXYiLCJzdWIiOiJqb3JnZWx1aXNAam9ndWVuY28uZGV2IiwiY2xpZW50IjoiOTk5OTk5OTk5OTk5OSIsIm5hbWUiOiJKb3JnZSBMdWlzIiwiZW1haWwiOiJqb3JnZXF1aWd1YW5nb0BvdXRsb29rLmNvbSIsInJvbGUiOiJNYW5hZ2VyIiwic2VydmljZSI6IlJlSWRpIiwibGltaXQiOjB9.X_g2Et9T3P_ZyCZcxB_esNfTlF7PzBYFIYTFSAJgeIo', 
9, 0);
INSERT INTO services (id,name,url,authentication_method,token,timeout,status) 
VALUES ('2', 'Authorize', 'http://localhost:8080', 
'X-API-KEY',
'api__6tpXYCxsXpCs7QeuI44KtoCq',
30, 0);

-- Tax Supports
delete from tax_supports;
INSERT INTO tax_supports (id, name) VALUES
('01', 'Crédito tributario declaración IVA servicios y bienes distintos de inventarios y activos fijos'),
('02', 'Costo o gasto para declaración IR servicios y bienes distintos de inventarios y activos fijos'),
('03', 'Activo fijo - crédito tributario para declaración de IVA'),
('04', 'Activo fijo - costo o gasto para declaración de IR'),
('05', 'Liquidación gastos de viaje, hospedaje y alimentación Gastos IR a nombre de empleados'),
('06', 'Inventario - crédito tributario para declaración de IVA'),
('07', 'Inventario - costo o gasto para declaración de IR'),
('08', 'Valor pagado para solicitar reembolso de gasto (intermediario)'),
('09', 'Reembolso por Siniestros'),
('10', 'Distribución de dividendos, beneficios o utilidades'),
('11', 'Convenios de débito o recaudación para IFIs'),
('12', 'Impuestos y retenciones presuntivos'),
('13', 'Valores reconocidos por entidades del sector público a favor de sujetos pasivos'),
('14', 'Valores facturados por socios a operadoras de transporte que no constituyen gasto de dicha operadora'),
('15', 'Pagos efectuados por consumos propios y de terceros de servicios digitales'),
('00', 'Casos especiales cuyo sustento no aplica en las opciones anteriores');

-- Document Type
delete from document_types;
INSERT INTO document_types (id, name, type, inventory) VALUES
('01', 'Factura', 'RUC', 'In'),
('02', 'Nota o boleta de venta', 'RUC', 'In'),
('03', 'Liquidación de compra de bienes o prestación de servicios', 'CI', 'In'),
('04', 'Nota de crédito', 'RUC', 'Out');

-- Service recharge for debit note
INSERT INTO roles(id, name, permissions) VALUES('xxx666_666xxx_x6x6x6', 'Recharge', $FILE{/com/unicenta/pos/templates/Role.Employee.xml} );
INSERT INTO people(id, name, apppassword, role, visible, image) VALUES ('xxx666_666xxx_x6x6x6', 'Recharge', NULL, 'xxx666_666xxx_x6x6x6', TRUE, NULL);

INSERT INTO categories(id, name) VALUES ('xxx666_666xxx_x6x6x6', 'Recharge');

INSERT INTO products(id, reference, code, name, category, taxcat, isservice, display, printto) 
VALUES ('xxx666_666xxx_x6x6x6', 'xxx666', 'xxx666', 'Recargos', 'xxx666_666xxx_x6x6x6', '015', 1, '<html><center>Recargos', '1');

INSERT INTO products_cat(product) VALUES ('xxx666_666xxx_x6x6x6');

INSERT INTO ticketsnum VALUES('ND', 'xxx666_666xxx_x6x6x6', '001201', 0, 'primary', 'Active');

-- Serie de la guia de remision (codDoc 06). Va en ticketsnum, no en
-- ticketsnum_purchase, porque se emite del lado de la venta.
INSERT INTO ticketsnum VALUES('GUI', '0', '001201', 0, 'primary', 'Active');
INSERT INTO ticketsnum VALUES('GUI', '1', '001301', 0, 'primary', 'Active');

INSERT INTO ticketsnum_purchase VALUES('LQ', '0', '001201', 0, 'primary', 'Active');
INSERT INTO ticketsnum_purchase VALUES('LQ', '1', '001301', 0, 'primary', 'Active');

-- Serie de la retencion. Va en la misma tabla que la liquidacion porque ambos son
-- documentos que el comprador emite. La llave primaria es (code, people_id), asi que
-- 'RT' lleva su propio contador, independiente del de 'LQ'.
INSERT INTO ticketsnum_purchase VALUES('RT', '0', '001201', 0, 'primary', 'Active');
INSERT INTO ticketsnum_purchase VALUES('RT', '1', '001301', 0, 'primary', 'Active');

-- Retenciones de IVA
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('1', '100% IVA HONORARIOS', 100, '3', 'IVA', '2004-06-17', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('2', '100% IVA OTROS', 100, '3', 'IVA', '2006-09-28', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('3', '30% IVA BIENES', 30, '1', 'IVA', '2006-10-02', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('4', '70% IVA SERVICIOS', 70, '2', 'IVA', '2007-02-03', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('5', '10% BIENES CONTR. ESPECIALES', 10, '9', 'IVA', '2015-04-08', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('6', '20% SERVICIOS CONTR. ESPECIALES', 20, '10', 'IVA', '2015-04-08', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('7', '0% IVA', 0, '7', 'IVA', '2015-06-02', 1);

-- Retenciones de RENTA
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('8', '8% SERVICIOS PREDOMINA INTELECTO', 8, '304', 'RENTA', '2009-02-16', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('9', '1% TRANSPORTE PASAJEROS Y CARGA', 1, '310', 'RENTA', '2009-03-25', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('10', '10% HONORARIOS PROFESIONALES', 10, '303', 'RENTA', '2010-07-16', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('11', '8% 304A COMISIONES Y PAGOS PRED INTEL NO RELAC. CON TIT.', 8, '304A', 'RENTA', '2015-03-02', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('12', '8% POR ARRENDAMIENTO BIENES INMUEBLES', 8, '320', 'RENTA', '2015-03-10', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('13', 'Otras retenciones incluye Microempresas', 1.75, '351', 'RENTA', '2020-10-05', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('14', '0% IVA en 0', 0, '7', 'RENTA', '2020-10-13', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('15', '0% IVA no procede', 0, '8', 'RENTA', '2020-10-13', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('16', '1% RIMPE', 1, '343', 'RENTA', '2022-01-19', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('17', '1.75% COMPRA BIENES AGRICOLA', 1.75, '312C', 'RENTA', '2020-05-15', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('18', '10% 304B NOTARIOS', 10, '304B', 'RENTA', '2015-03-10', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('19', '5% Servicios profesionales sociedades', 5, '303A', 'RENTA', '2022-01-19', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('20', '2% TRANSFERENCIA DE BIENES MUEBLES', 2, '312', 'RENTA', '2020-04-01', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('21', '3% SERVICIOS PREDOMINA M.O.', 3, '307', 'RENTA', '2009-02-16', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('22', '3% SERVICIOS PUBLICIDAD Y COMUNICA', 3, '309', 'RENTA', '2009-02-16', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('23', '3% 311 PAGOS CON LIQUIDACIONES DE COMPRA', 3, '311', 'RENTA', '2015-03-10', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('24', '2% Seguros y reaseguros', 2, '322', 'RENTA', '2020-04-17', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('25', '3% OTRAS RETENCIONES', 3, '3440', 'RENTA', '2020-04-01', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('26', '0% - 332 OTRAS COMPRAS DE BIENES NO SUJETAS', 0, '332', 'RENTA', '2012-03-20', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('27', '0% - 332I Pago convenio de debito', 0, '332I', 'RENTA', '2019-07-26', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('28', '0% - 332G Pagos tarjeta de crédito', 0, '332G', 'RENTA', '2019-07-26', 1);
INSERT INTO withhold_taxes(id, name, percentage, code, tax_type, created_at, status) VALUES ('29', '50% IVA', 50, '11', 'IVA', '2020-04-01', 1);

INSERT INTO dispatchers (id,taxid_type,taxid,name,plate,created_at) VALUES ('9999999999999','CF','9999999999999','Dispatcher','ZZZ999', SYSDATE());
