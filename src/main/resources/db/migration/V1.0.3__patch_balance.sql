CREATE OR REPLACE FUNCTION add_random_xp(id BIGINT)
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
	xp INT;
	bonus_xp INT;
	karma INT;
BEGIN
   SELECT get_random_xp INTO xp FROM get_random_xp();
	SELECT users.karma_points INTO karma FROM users WHERE users.user_id = id;
	bonus_xp := 0;
	IF karma < 0 THEN
		bonus_xp := -1;
	ELSIF karma >= 100 THEN
		bonus_xp := 1;
	END IF;
	IF (SELECT EXISTS(SELECT 1 FROM transactions JOIN items ON transactions.item_id = items.item_id WHERE transactions.user_id=id AND type_id = 1)) THEN
	    bonus_xp := bonus_xp + 1;
	END IF;
	SELECT INTO rank_changed, previous_rank, current_rank, next_rank, current_xp * FROM add_xp(id, xp + bonus_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;
