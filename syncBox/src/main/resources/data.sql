INSERT INTO roles(
    id, name, created, updated, status
) VALUES
(1, 'ROLE_USER', NOW(), NOW(), 'ACTIVE'),
(2, 'ROLE_ADMIN', NOW(), NOW(), 'ACTIVE');

INSERT INTO users(
    id,
    created,
    updated,
    status,
    username,
    first_name,
    last_name,
    email,
    password
) VALUES
    --admin/admin
    (1, NOW(), NOW(), 'ACTIVE', 'admin', 'admin', 'admin', 'admin@localhost',
     '$2a$04$.09kYd8b8jvedKVFrSrvRuyCVILYwc/.NVZ3QvxGXYvzoq5PEo3PC'
     ),
         --test/test
    (2, NOW(), NOW(), 'ACTIVE', 'test', 'test', 'test', 'test@localhost',
     '$2a$04$0x6d.m8FVxuF5eBg3TqlheZvYO0dGtvZ8dR7PjDuEN18VTW83s7gi'
    )
    ;

 INSERT INTO user_roles(user_id, role_id)
 VALUES (1, 2), (2, 1);