db.query_test_collection.find().forEach(
    function(x) {
        if (x.text !== undefined) {
            db.test.update(
                    {_id: x._id},
                    {$set : {_keyword : x.text.replace(/[\.,-\/#!$%\^&\*;:{}=\-_`~()@""'']/g,"").toLowerCase().split(" ")}}
            );
        }
    }
);
