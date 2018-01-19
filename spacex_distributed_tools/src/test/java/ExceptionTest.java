class ExceptionSrc{
    public void fun0(){
        return;
    }
    public int fun1(){
        //String a = fun0();
        try {
            return 2 / 0;
        }catch (Exception e){
            return -1;
        }
    }
}
public class ExceptionTest {
    public static void fun2(){
        ExceptionSrc exceptionSrc = new ExceptionSrc();
        try {
            exceptionSrc.fun1();
        }catch (Exception e){
            System.out.println("======");
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        fun2();
    }
}
