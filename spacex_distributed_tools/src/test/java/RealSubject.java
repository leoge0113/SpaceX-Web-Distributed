
interface Subject {
    public abstract void request();

    public abstract void request1();
}


public class RealSubject implements Subject {//具体实现类

    public RealSubject() {

    }

    public void request1() {
        System.out.println("From Real Subject");
    }

    public void request() {
        System.out.println("From Real Subject");
    }
}
