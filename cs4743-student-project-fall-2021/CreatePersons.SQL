DROP PROCEDURE CreatePersons;
DELIMITER $$
CREATE PROCEDURE CreatePersons()
BEGIN

	DECLARE x INT;

	SET x = 1;

	loop_label: LOOP
		

		-- Randomizes dates
		set @birthDate := (SELECT FROM_UNIXTIME(
			UNIX_TIMESTAMP('1970-01-01') + FLOOR(
				RAND() * (
					UNIX_TIMESTAMP('2020-01-01') - UNIX_TIMESTAMP('1970-01-01') + 1
				)
			)
		));

		select DATE(@birthDate);

		select current_date;

		Set @age = floor(DATEDIFF(current_date, DATE(@birthDate))/365);

		select @age;

		-- Randomizes firstname
		set @firstName :=
		(select firstname
		from firstnames
		order by rand()
		limit 1);

		select @firstName;

		-- Randomizes lastname
		set @lastName :=
		(select lastname
		from lastnames
		order by rand()
		limit 1);

		select @lastName;
		
		-- Creates the Person

		INSERT into people (first_name, last_name,dob,age)
		values(@firstName,@lastName,@birthDate,@age);

		Set @personId = (SELECT LAST_INSERT_ID());

		-- Creates the Audit for the Person under Ragnar
		
		INSERT into audit(change_msg,changed_by,person_id,when_occurred)
		values('added',1,@personId,current_timestamp);
		
		SET x = x + 1;
		
		IF x > 10000 THEN
			LEAVE loop_label;
		END IF;
	END LOOP;
END$$
DELIMITER ;