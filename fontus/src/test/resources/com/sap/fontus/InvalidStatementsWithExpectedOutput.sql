INSERT INTO customers (name, vorname) VALUES ('Max', 'Mustermann'); insert into users VALUES ('peter') returning id;
INSERT INTO customers (name, `__taint__name`, vorname, `__taint__vorname`) VALUES ('Max', '0', 'Mustermann', '0');
INSERT INTO users VALUES ('peter', '0') RETURNING id, `__taint__id`;

insert into users VALUES ('peter') returning id, name, 'Max' || vorname;
INSERT INTO users VALUES ('peter', '0') RETURNING id, `__taint__id`, name, `__taint__name`, 'Max' || vorname, '0' || `__taint__vorname`;

INSERT INTO `building` VALUES ('Building 1'),('Building 2'),('Building 3'),('Building 4'),('Building 5'),('Building 6'),('Building 7'),('Building 8'),('Building 9');
INSERT INTO `building` VALUES ('Building 1', '0'), ('Building 2', '0'), ('Building 3', '0'), ('Building 4', '0'), ('Building 5', '0'), ('Building 6', '0'), ('Building 7', '0'), ('Building 8', '0'), ('Building 9', '0');

INSERT INTO `floor` VALUES ('Fifth Floor'),('First Basement Floor'),('First Floor'),('Fourth Floor'),('Ground Floor'),('Second Basement Floor'),('Second Floor'),('Sixth Floor'),('Third Floor');
INSERT INTO `floor` VALUES ('Fifth Floor', '0'), ('First Basement Floor', '0'), ('First Floor', '0'), ('Fourth Floor', '0'), ('Ground Floor', '0'), ('Second Basement Floor', '0'), ('Second Floor', '0'), ('Sixth Floor', '0'), ('Third Floor', '0');