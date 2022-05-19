package zcc.TicketViewer;

import org.junit.Test;

import java.io.IOException;

public class TicketGetterTest
{
    private TicketGetter tg = new TicketGetter("","","");

    public boolean testURL(String url, int start, int end, String target) throws IOException {
        String str = tg.returnJSONfromURL(url).substring(start,end);
        System.out.println("==========================================================");
        System.out.println("Testing getting content from "+url+"\nResponses should match the target string");
        System.out.println("Target String: "+target);
        System.out.println("Extracted String: "+str);
        if (target.equals(str))
            return true;

        return false; //if this line is reached then failed
    }
    @Test
    public void testResponseIntegrity00() throws IOException
    {
        assert(testURL("https://www.google.com/drive/",2,5,"DOC"));
    }

    @Test
    public void testResponseIntegrity01() throws IOException
    {
        assert(testURL("https://www.zendesk.com/",2,5,"doc"));
    }

    @Test
    public void testResponseIntegrity02() throws IOException
    {
        assert(testURL("https://example.zendesk.com/api/v2/tickets.json?page[size]=25",2,7,"error"));
    }
/*
    @Test
    public void testResponseIntegrityIntegrity() throws IOException
    {
        //This should return a FAILED test. Target string should be "HTML" to pass
        assert(testURL("https://www.minecraft.net/en-us",1,5,"FAIL"));
    }
 */
}
