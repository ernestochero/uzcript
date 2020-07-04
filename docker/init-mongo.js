// 1. create user on dataBase
db.createUser(
    {
        user: "uzcript_username",
        pwd: "uzcript_password",
        roles: [
            {
                role: "readWrite",
                db: "uzcript"
            }
        ]
    }
)

// 2. insert fake data to courses collection
var users_collection_name = "users";
var users_data_test = [
    {
        "address": "1001",
        "name" : "ernesto",
        "lastName" : "chero"
    }
];
db.createCollection(users_collection_name);
users_data_test.forEach(function (user) {
    db.getCollection(users_collection_name).insertOne(user);
});

