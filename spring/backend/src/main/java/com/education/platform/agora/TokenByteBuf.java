package com.education.platform.agora;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

final class TokenByteBuf {

    private ByteBuffer buffer;

    TokenByteBuf() {
        this.buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
    }

    TokenByteBuf(byte[] bytes) {
        this.buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    byte[] asBytes() {
        byte[] out = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(out, 0, out.length);
        return out;
    }

    TokenByteBuf put(short value) {
        buffer.putShort(value);
        return this;
    }

    TokenByteBuf put(int value) {
        buffer.putInt(value);
        return this;
    }

    TokenByteBuf put(byte[] value) {
        put((short) value.length);
        buffer.put(value);
        return this;
    }

    TokenByteBuf put(String value) {
        return put(value.getBytes(StandardCharsets.UTF_8));
    }

    TokenByteBuf putIntMap(TreeMap<Short, Integer> map) {
        put((short) map.size());
        for (Map.Entry<Short, Integer> pair : map.entrySet()) {
            put(pair.getKey());
            put(pair.getValue());
        }
        return this;
    }

    short readShort() {
        return buffer.getShort();
    }

    int readInt() {
        return buffer.getInt();
    }

    byte[] readBytes() {
        short length = readShort();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    String readString() {
        return new String(readBytes(), StandardCharsets.UTF_8);
    }
}
