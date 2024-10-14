CREATE DATABASE IF NOT EXISTS petclinic;

ALTER DATABASE petclinic
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

-- First, create the user
CREATE USER IF NOT EXISTS 'petclinic'@'%' IDENTIFIED BY 'petclinic';

-- Then, grant privileges to the user
GRANT ALL PRIVILEGES ON petclinic.* TO 'petclinic'@'%';
