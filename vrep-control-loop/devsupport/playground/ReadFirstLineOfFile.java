package playground;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadFirstLineOfFile
{
    public static void main(String[] args)
    {
        String fileName = args[0];
        Path path = Paths.get(fileName);
        try
        {
            FileInputStream fileInputStream = new FileInputStream(path.toFile());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            Reader reader = new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8);
            char character;
            int cnt = 0;
            StringBuilder builder = new StringBuilder();
            while(cnt < 10000)
            {
                character = (char)reader.read();
                builder.append(character);
                cnt++;
            }
            System.out.println(builder.toString());
            reader.close();
        }
        catch (FileNotFoundException exc)
        {
            exc.printStackTrace();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
