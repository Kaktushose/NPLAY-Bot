CREATE OR REPLACE FUNCTION update_rank_statistics()
RETURNS VOID AS
$$
DECLARE
	_users_with_boosters INT;
	_users_with_premium INT;
	_daily_info_users INT;
	_starboard_count INT;
	_active_karma_users INT;
	_rank_1_count INT;
	_rank_2_count INT;
	_rank_3_count INT;
	_rank_4_count INT;
	_rank_5_count INT;
	_rank_6_count INT;
	_rank_7_count INT;
	_rank_8_count INT;
	_rank_9_count INT;
	_rank_10_count INT;
	_rank_11_count INT;
	_rank_12_count INT;
	_rank_13_count INT;
	_rank_14_count INT;
	_rank_15_count INT;
BEGIN
	SELECT COUNT(*) FROM transactions JOIN items ON transactions.item_id = items.item_id JOIN item_types ON items.type_id = 1 WHERE base_type_id = 1 INTO _users_with_boosters;
	SELECT COUNT(*) FROM transactions JOIN items ON transactions.item_id = items.item_id JOIN item_types ON items.type_id = 2 WHERE base_type_id = 2 INTO _users_with_premium;
	SELECT COUNT(*) FROM users WHERE daily_message = TRUE INTO _daily_info_users;
	SELECT COUNT(*) FROM starboard_entries INTO _starboard_count;
	SELECT COUNT(*) FROM users WHERE karma_tokens < 5 INTO _active_karma_users;
	SELECT COUNT(*) FROM users WHERE rank_id = 1 INTO _rank_1_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 2 INTO _rank_2_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 3 INTO _rank_3_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 4 INTO _rank_4_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 5 INTO _rank_5_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 6 INTO _rank_6_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 7 INTO _rank_7_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 8 INTO _rank_8_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 9 INTO _rank_9_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 10 INTO _rank_10_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 11 INTO _rank_11_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 12 INTO _rank_12_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 13 INTO _rank_13_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 14 INTO _rank_14_count;
	SELECT COUNT(*) FROM users WHERE rank_id = 15 INTO _rank_15_count;

	INSERT INTO rank_statistics(
		"date",
		"users_with_boosters",
		"users_with_premium",
		"daily_info_users",
		"starboard_count",
		"active_karma_users",
		"rank_1_count",
		"rank_2_count",
		"rank_3_count",
		"rank_4_count",
		"rank_5_count",
		"rank_6_count",
		"rank_7_count",
		"rank_8_count",
		"rank_9_count",
		"rank_10_count",
		"rank_11_count",
		"rank_12_count",
		"rank_13_count",
		"rank_14_count",
		"rank_15_count"
	)
	VALUES(
		CURRENT_DATE,
	 	_users_with_boosters,
		_users_with_premium,
		_daily_info_users,
		_starboard_count,
		_active_karma_users,
		_rank_1_count,
		_rank_2_count,
		_rank_3_count,
		_rank_4_count,
		_rank_5_count,
		_rank_6_count,
		_rank_7_count,
		_rank_8_count,
		_rank_9_count,
		_rank_10_count,
		_rank_11_count,
		_rank_12_count,
		_rank_13_count,
		_rank_14_count,
		_rank_15_count
	)
	ON CONFLICT (DATE) DO UPDATE SET
		users_with_boosters = _users_with_boosters,
		users_with_premium = _users_with_premium,
		daily_info_users = _daily_info_users,
		starboard_count = _starboard_count,
		active_karma_users = _active_karma_users,
		rank_1_count = _rank_1_count,
		rank_2_count = _rank_2_count,
		rank_3_count = _rank_3_count,
		rank_4_count = _rank_4_count,
		rank_5_count = _rank_5_count,
		rank_6_count = _rank_6_count,
		rank_7_count = _rank_7_count,
		rank_8_count = _rank_8_count,
		rank_9_count = _rank_9_count,
		rank_10_count = _rank_10_count,
		rank_11_count = _rank_11_count,
		rank_12_count = _rank_12_count,
		rank_13_count = _rank_13_count,
		rank_14_count = _rank_14_count,
		rank_15_count = _rank_15_count;
END;
$$ LANGUAGE plpgsql;
