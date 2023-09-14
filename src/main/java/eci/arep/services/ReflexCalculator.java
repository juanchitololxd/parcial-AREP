package eci.arep.services;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

public class ReflexCalculator{

    public Double calculate(String op, Double[] args) throws  IllegalAccessException, InvocationTargetException{
        Double dResult = null;
        try {
            Class<?> cls = Class.forName("java.lang.Math");
            Method[] methods = cls.getDeclaredMethods();

            boolean invoked = false;
            int i = 0;
            while(!invoked && i < methods.length){
                if (op.toLowerCase(Locale.ROOT).equals(methods[i].getName().toLowerCase(Locale.ROOT))){
                    System.out.println(Arrays.toString(args));
                    dResult = (Double) methods[i].invoke(null, args);
                    invoked = true;
                }
                i++;

            }
        }catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }

        return dResult;
    }

    public Double[] quicksort(Double[] numbers) {
        // TODO implementar quicksort
        return numbers;
    }
}
