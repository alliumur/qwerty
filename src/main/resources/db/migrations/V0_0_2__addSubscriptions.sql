create table user_subscriptions(
    channel_id int8 not null,
    subscriber_id int8 not null,
    primary key (channel_id, subscriber_id),
    constraint foreign key (channel_id) references user(id),
    constraint foreign key (subscriber_id) references user(id)
)