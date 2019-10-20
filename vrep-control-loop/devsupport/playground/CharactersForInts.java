package playground;

public class CharactersForInts
{
    public static void main(String[] args)
    {
        for(char cIdx = Character.MIN_VALUE; cIdx < 300; cIdx++)
        {
            String getName = Character.getName(cIdx);
            String toString = Character.toString(cIdx);
            System.out.format("idx: %c, getName: %s, toString: %s\n", cIdx, getName, toString);
        }
    }
}
