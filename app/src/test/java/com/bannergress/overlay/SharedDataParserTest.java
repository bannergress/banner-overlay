package com.bannergress.overlay;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class SharedDataParserTest {
    @Test
    public void testParse() {
        String data1 = "MD 2022: Donostia - Bannergress\nhttps://bannergress.com/banner/md-2022-donostia-ff41";
        SharedDataParser.ParsedData result1 = SharedDataParser.parse(data1);
        assertThat(result1.type, is(SharedDataParser.ParsedDataType.banner));
        assertThat(result1.id, is("md-2022-donostia-ff41"));

        String data2 = "Auf Intel zeigen\nhttps://intel.ingress.com/mission/edd0df3599e24480bd7b7049fbda3a32.1c";
        SharedDataParser.ParsedData result2 = SharedDataParser.parse(data2);
        assertThat(result2.type, is(SharedDataParser.ParsedDataType.mission));
        assertThat(result2.id, is("edd0df3599e24480bd7b7049fbda3a32.1c"));

        String data3 = "Im Scanner zeigen\nhttps://link.ingress.com/?link=https%3a%2f%2fintel.ingress.com%2fmission%2fedd0df3599e24480bd7b7049fbda3a32.1c&apn=com.nianticproject.ingress&isi=576505181&ibi=com.google.ingress&ifl=https%3a%2f%2fapps.apple.com%2fapp%2fingress%2fid576505181&ofl=https%3a%2f%2fintel.ingress.com%2fmission%2fedd0df3599e24480bd7b7049fbda3a32.1c";
        SharedDataParser.ParsedData result3 = SharedDataParser.parse(data3);
        assertThat(result3.type, is(SharedDataParser.ParsedDataType.mission));
        assertThat(result3.id, is("edd0df3599e24480bd7b7049fbda3a32.1c"));
    }
}
