local JSON = require("JSON")

__storage = __storage or {}

-- get table by name
local table_name = '%s'

local db_table = __storage[table_name]

if db_table then
    local data = db_table:findAll()

    return JSON:encode(data)
end