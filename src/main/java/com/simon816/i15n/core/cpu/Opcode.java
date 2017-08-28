package com.simon816.i15n.core.cpu;

public enum Opcode {
    /**
     * Add with carry
     */
    ADC
    
    /**
     * Logical AND
     */
    , AND
    
    /**
     * Arithmetic shift left
     */
    , ASL_ACC, ASL_MEM
    
    /**
     * Branch on carry clear
     */
    , BCC
    
    /**
     * Branch on carry set
     */
    , BCS

    // /**
    // * No operation
    // */
    // ,NOP
    ;

    private static final Opcode[] LOOKUP = Opcode.values();

    public static Opcode from(byte opcode) {
        return LOOKUP[opcode];
    }

    public byte code() {
        return (byte) ordinal();
    }

    public static Opcode get(int insn) {
        return LOOKUP[insn];
    }

}
