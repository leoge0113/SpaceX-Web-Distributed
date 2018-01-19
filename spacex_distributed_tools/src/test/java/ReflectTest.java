import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class MyClass {
    public int count;

    public MyClass(int start) {
        count = start;
    }

    public void increase(int step) {
        count = count + step;
        System.out.println("Increase -> " + count);
    }
}

public class ReflectTest {
    public static void main(String[] args) {
        MyClass myClass = new MyClass(0); //一般做法
        myClass.increase(2);
        System.out.println("Normal -> " + myClass.count);
        try {
            Constructor constructor = MyClass.class.getConstructor(int.class); //获取构造方法
            MyClass myClassReflect = (MyClass) constructor.newInstance(10); //创建对象
            Method method = MyClass.class.getMethod("increase", int.class);  //获取方法

            method.invoke(myClassReflect, 5); //调用方法
            Field field = MyClass.class.getField("count"); //获取域
            System.out.println("Reflect -> " + field.getInt(myClassReflect)); //获取域的值
        } catch (Exception e) {
            e.printStackTrace();
        }
        //spring 提供的反射封装
        try {
            ReflectionUtils.makeAccessible(MyClass.class.getMethod("increase", int.class));
            MyClass.class.getMethod("increase", int.class).invoke(new MyClass(1),3);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
