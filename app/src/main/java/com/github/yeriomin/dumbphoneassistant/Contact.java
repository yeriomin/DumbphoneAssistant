package com.github.yeriomin.dumbphoneassistant;

import java.io.UnsupportedEncodingException;

public class Contact {

    private String id = null;
    private String name;
    private String number;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected Contact(String id, String name, String number) {
        this.id = id;
        this.name = name;
        this.number = number;
    }

    /**
     * Null-safe string compare
     */
    private boolean compareStrings(final String one, final String two) {
        if (one == null ^ two == null) {
            return false;
        }
        if (one == null && two == null) {
            return true;
        }
        return one.compareTo(two) == 0;
    }

    @Override
    public boolean equals(Object o) {
        // if not Contact, can't be true
        if(!(o instanceof Contact)) 
            return false;
        Contact c = (Contact)o;
        
        // only if id's present, compare them
        if((id != null) && (id.length()) > 0 && (c.id.length() > 0))
            return c.id.compareTo(id) == 0;
        
        // if SimNames not equal...
        if(!compareStrings(name, c.name)) {
            return false;
        }

        // finally if numbers not equal...
        return compareStrings(number, c.number);
    }

    public static byte[] getSimCompatibleByteArray(String src) {
        byte[] dest = null;
        try {
            byte[] srcByte = src.getBytes("UTF-16BE");
            dest = ucs2ToAlphaField(srcByte, 0, srcByte.length, 0, dest);
            System.out.println("Before conversion: ");
            System.out.println("String: " + src);
            System.out.print("Bytes : ");
            for (int i = 0; i < srcByte.length; i++) {
                System.out.print(Integer.toHexString(srcByte[i] & 0xFF) + " ");
            }
            System.out.println();
            System.out.println("After conversion:");
            System.out.println("String: " + String.valueOf(dest));
            System.out.print("Bytes : ");
            for (int i = 0; i < dest.length; i++) {
                System.out.print(Integer.toHexString(dest[i] & 0xFF) + " ");
            }
            System.out.println();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * The function will give priority to 81 and 82 using the format code, if you do not meet the encoding condition, choose 80 format code
     *
     * @param src source UCS2 byte array
     * @param srcOff source array of bytes starting position
     * @param srcLen source byte array length
     * @param destOff The starting position of the target array destOff
     * @return The target array
     */
    private static byte[] ucs2ToAlphaField(byte[] src, int srcOff, int srcLen, int destOff, byte dest[]) {
        int i;
        int min = 0x7FFF;
        int max = 0;
        int temp;
        int outOff;
        // When the source array length is greater than 2 to 81 or 82 formats
        if (srcLen > 2) {
            // The minimum and maximum values of the source array
            for (i = 0; i < srcLen; i += 2) {
                //Each character is first bytes is not 0
                if (src[srcOff + i] != 0) {
                    temp = ((src[srcOff + i] << 8) & 0xFF00) | (src[srcOff
                            + i + 1] & 0xFF);
                    // Because in the 81 format base left 7 after highs to fill 0, so FFFF cannot use 8000 to 81
                    if (temp < 0) {
                        max = min + 130;
                        break;
                    }
                    if (min > temp) {
                        min = temp;
                    }
                    if (max < temp) {
                        max = temp;
                    }
                }
            }
        }
        // If all the characters in the UCS code in a continuous range of 128, then the first bytes are the same
        if ((max - min) < 129) {
            // If the fifteenth bit to eighth bit the same, you can use 81 format code
            if ((byte) (min & 0x80) == (byte) (max & 0x80)) {
                // Figure 81 the length of the target array format, initialization
                dest = new byte[srcLen / 2 + 3];
                // Setting the target array of second byte length
                dest[destOff + 1] = (byte) (srcLen / 2);
                // Set the first byte is 81
                dest[destOff] = (byte) 0x81;
                // The base is fifteenth to eighth,
                min = min & 0x7F80;
                dest[destOff + 2] = (byte) ((min >> 7) & 0xFF);
                outOff = destOff + 3;
            }
            // Eighth bytes are not the same (a 0, a 1), then use the 82 format
            else {
                // Figure 82 the length of the target array format, initialization
                dest = new byte[srcLen / 2 + 4];
                // Setting the target array of second byte length
                dest[destOff + 1] = (byte) (srcLen / 2);
                // Set the first byte is 82
                dest[destOff] = (byte) 0x82;
                // The base for the source array minimum value (first bytes to 0 except)
                dest[destOff + 2] = (byte) ((min >> 8) & 0xFF);
                dest[destOff + 3] = (byte) (min & 0xFF);
                outOff = destOff + 4;
            }

            for (i = 0; i < srcLen; i += 2) {
                // If the first byte is 0, then 7 to 1, the highest for 0
                if (src[srcOff + i] == 0) {
                    dest[outOff] = (byte) (src[srcOff + i + 1] & 0x7F);
                }
                // If the first byte is not 0, using the UCS code is subtracted from the base, the high fill 1
                else {
                    temp = (((src[srcOff + i] << 8) & 0xFF00) | (src[srcOff
                            + i + 1] & 0xFF)) - min;
                    dest[outOff] = (byte) (temp | 0x80);
                }
                outOff++;
            }
            // Returns the target array
            return dest;
        }

        // Do not meet the 81 and the 82 format, using 80 format code
        dest = new byte[srcLen + 1];
        dest[destOff] = (byte) 0x80;
        System.arraycopy(src, 0, dest, 1, srcLen);
        // Returns a 80 format object array
        return dest;
    }
}