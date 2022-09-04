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

        String data4 = "https://bannergress.com/banner/%E5%B1%B1%E8%83%8C%E5%8F%A4%E9%81%93%E3%82%92%E6%AD%A9%E3%81%8F-1304";
        SharedDataParser.ParsedData result4 = SharedDataParser.parse(data4);
        assertThat(result4.type, is(SharedDataParser.ParsedDataType.banner));
        assertThat(result4.id, is("山背古道を歩く-1304"));

        String data5 = "https://bannergress.com/banner/山背古道を歩く-1304";
        SharedDataParser.ParsedData result5 = SharedDataParser.parse(data5);
        assertThat(result5.type, is(SharedDataParser.ParsedDataType.banner));
        assertThat(result5.id, is("山背古道を歩く-1304"));

        String data6 = "https://bannergress.com/banner/md-2022-donostia-ff41?a=b#c";
        SharedDataParser.ParsedData result6 = SharedDataParser.parse(data6);
        assertThat(result6.type, is(SharedDataParser.ParsedDataType.banner));
        assertThat(result6.id, is("md-2022-donostia-ff41"));

        String data7 = "bannergress.com/banner/md-2022-donostia-ff41";
        SharedDataParser.ParsedData result7 = SharedDataParser.parse(data7);
        assertThat(result7.type, is(SharedDataParser.ParsedDataType.invalid));
    }
}
