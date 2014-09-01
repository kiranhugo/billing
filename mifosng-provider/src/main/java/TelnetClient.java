

class Jasper
{
    public static void main(String args[]) throws Exception
    {

	
    	String line="3,31;August;2014,Migration,CREDIT,120,ok;;";
    	
    	System.out.println(line);
    	String[] strings=line.split(",");
    	for(int i=0;i<strings.length;i++){
    		System.out.println(strings[i]);
    	}
    }
}