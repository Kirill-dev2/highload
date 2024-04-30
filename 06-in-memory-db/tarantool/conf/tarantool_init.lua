function start()
    box.schema.space.create('chat_messages', { if_not_exists = true })
    box.space.chat_messages:create_index('primary', { type = "TREE", unique = true, parts = { 1, 'string' }, if_not_exists = true })
    box.space.chat_messages:create_index('hash', { type = "TREE", unique = false, parts = { 2, 'number' }, if_not_exists = true })
end

function search(hash)
    return box.space.chat_messages.index.hash:select({hash}, {iterator = EQ})
end

function save(id, hash, data)
    return box.space.chat_messages:insert({id, hash, data, 6})
end