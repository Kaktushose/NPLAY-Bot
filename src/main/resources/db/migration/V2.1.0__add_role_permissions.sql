CREATE TABLE role_permissions (
    role_id BIGINT PRIMARY KEY NOT NULL,
    permissions INTEGER NOT NULL DEFAULT 1;
);

CREATE FUNCTION get_role_permissions(ids BIGINT[])
RETURNS TABLE (permissions INT) AS
$$
DECLARE
	combined_permissions INTEGER := 0;
	role_permission INTEGER;
	id BIGINT;
BEGIN
    FOREACH id IN ARRAY ids LOOP
        SELECT role_permissions.permissions INTO role_permission FROM role_permissions WHERE role_permissions.role_id = id;
        IF NOT FOUND THEN
            CONTINUE;
        END IF;
        combined_permissions := combined_permissions | role_permission;
    END LOOP;
	RETURN QUERY SELECT combined_permissions;
END;
$$ LANGUAGE plpgsql;
