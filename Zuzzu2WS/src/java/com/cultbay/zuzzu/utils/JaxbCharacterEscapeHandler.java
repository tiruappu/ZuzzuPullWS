/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cultbay.zuzzu.utils;

/**
 *
 * @author kondalarao
 */
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;

public class JaxbCharacterEscapeHandler implements CharacterEscapeHandler {

    public void escape(char[] buf, int start, int len, boolean isAttValue,
            Writer out) throws IOException {

        for (int i = start; i < start + len; i++) {
            char ch = buf[i];
            out.write(ch);
        }
    }
}