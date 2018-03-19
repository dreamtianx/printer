package utils;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yt on 2017/1/24.
 */

public class P {


    static int depth = 0;// 用于控制当前的递归深度。

    static int depthLimit = 3;// 默认的递归深度

    public static void printList(List list) {
        System.out.println(printList(list, depthLimit, false));
    }

    public static void printList(List list, boolean isLn) {
        System.out.println(printList(list, depthLimit, isLn));
    }

    public static <T> void print(T obj) {
        if (obj instanceof List) {
            printList((List) obj);
        } else if (obj instanceof Map) {
            printMap((Map) obj);
        } else if (obj.getClass().isArray()) {
            printArray((Object[]) obj);
        } else
            printObj(obj);
    }


    public static String printList(List list, int depthLimit, boolean isLn) {

        P.depthLimit = depthLimit;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("size :" + list.size());

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (isLn)
                stringBuilder.append('\n');

            stringBuilder.append('\n');
            stringBuilder.append("index " + i + " ");

            if (isLn) {
                stringBuilder.append('\n');
                stringBuilder.append('\n');
            }
            stringBuilder.append(obj != null ? (checkType(obj.getClass()) ? String.valueOf(obj) : printObj(obj, isLn)) : "null");
        }
        return stringBuilder.toString();
    }


    public static String printArray(Object[] objects) {
        StringBuilder stringBuilder = new StringBuilder();
        // stringBuilder.append("size :" + objects.length );
        //  stringBuilder.append("[");

        for (Object object : objects) {
            stringBuilder.append("[");
            // 这里要好关 ， 如果是 基本类型的话 就直接使用。 不用跑obj。
            stringBuilder.append(object != null ? (checkType(object.getClass()) ? String.valueOf(object) : printObj(object, false)) : "null");
            stringBuilder.append("]").append(",");
        }

        // 要做为空判断 以及处理最后一个字符
        if (stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        // stringBuilder.append("]");

        return stringBuilder.toString();
    }


    public static void printMap(Map map) {
        System.out.println(printMap1(map));
    }

    public static String printMap1(Map map) {
        StringBuilder stringBuilder = new StringBuilder();
        if (map != null && map.size() > 0) {
            Iterator entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String key = (String) entry.getKey();
                Object obj = (Object) entry.getValue();

                //  这里注释掉的原因 是 如果map 包含list的话 会直接进入printObj 把list 处理掉。
                if (obj != null)
//                    if (obj.getClass().getName().equals("java.util.List"))
//                        stringBuilder.append(key + ":").append("size=" + ((List) obj).size());
//                    else
                    stringBuilder.append(key + ":").append(checkType(obj.getClass()) ? obj : printObj(obj, false)).append(",");
                else
                    stringBuilder.append(key + ":null");
            }

            if (stringBuilder.lastIndexOf(",") == stringBuilder.length() - 1)
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }


// 不考虑 两种输出情况。 list嵌套list 。 map嵌套list 。 这两种情况都只输出大小。  只考虑普通对象中包含的list。

    //  printObj 不能直接拿 list 和map 做参数。

    public static void printObj(Object object) {
        System.out.println(printObj(object, true));
    }


    public static String printObj(Object object, boolean isLn) {  //  isLn 这个折行 是指 。 根元素下每个对象折行。  进这个方法 对象不为空 由调用者去判断组织输出。

        StringBuilder stringBuilder = new StringBuilder();
        if (object == null) {
            stringBuilder.append("null");
            return stringBuilder.toString();
        }

        Field[] fields = object.getClass().getDeclaredFields();
        if (object instanceof Map) {
            stringBuilder.append("[Map]@" + " {" + printMap1((Map) object) + "} ");
        }
        // 如果传过来的是集合对象，那就只显示大小， 不允许list  map  array嵌套list
        else if (object instanceof List) {
            stringBuilder.append("[List]@" + " {size=" + ((List) object).size() + "} ");
        } else if (object.getClass().isArray()) {
            stringBuilder.append(printArray((Object[]) object)).append("");
        } else if (checkType(object.getClass())) {
            stringBuilder.append(object != null ? object : "null");
        } else {
            for (Field filed : fields) {    // 遍历对象的每个属性
                Class<?> aClass = filed.getType();
                try {
                    filed.setAccessible(true);
                    if (checkType(aClass)) {
                        String a = stringBuilder.toString();
                        if (a.length() > 0 && !a.substring(a.length() - 1, a.length()).equals("<"))
                            stringBuilder.append(" ");

                        stringBuilder.append("" + filed.getName() + " : ").append(filed.get(object) != null ? filed.get(object).toString() : "null");

                    } else if (aClass.getName().equals("java.util.List")) {
                        List list = (List) filed.get(object);

                        stringBuilder.append("[List]@" + filed.getName() + "{").append(filed.get(object) != null ? "size:" + list.size() : "null").append("} ");

                    } else if (aClass.getName().equals("java.util.HashMap") ||
                            aClass.getName().equals("java.util.Map")) {
                        Map map = (Map) filed.get(object);
                        stringBuilder.append("[Map]@" + filed.getName() + " {").append(filed.get(object) != null ? printMap1((Map) filed.get(object)) : "null").append("} ");
                    } else if (aClass.isArray()) {
                        // 如果是空 就是[]
                        stringBuilder.append("[Array]@" + filed.getName() + " [").append(filed.get(object) != null ? printArray((Object[]) filed.get(object)) : "null").append("] ");
                    } else if (object instanceof Object) {
                        stringBuilder.append(" @" + filed.getName() + "<").append(filed.get(object) != null ? printObj(filed.get(object), false) : "null").append("> ");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isLn)
                    stringBuilder.append('\n');
            }
            // 先遍历完整个对象。 然后再去做递归读取 list对象。为了把list放在最后显示。
            if (depthLimit > 1)
                for (Field filed : fields) {
                    Class<?> aClass = filed.getType();
                    if (aClass.getName().equals("java.util.List")) {

                        depth++;
                        StringBuilder stringBuilder2 = new StringBuilder();

                        List list = null;
                        try {
                            list = (List) filed.get(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        //在深度之内
                        if (depth < depthLimit) {
                            if (list != null && list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    Object obj = list.get(i);
                                    if (list.get(i) != null) {
                                        // 递归前的折行
                                        stringBuilder2.append('\n');
                                        StringBuilder stringBuilder3 = new StringBuilder();
                                        // 根据递归深度 来添加前面 的空格。
                                        for (int j = 0; j < depth; j++) {
                                            stringBuilder3.append("    ");
                                        }
                                        stringBuilder2.append(stringBuilder3);
                                        //从这里开始执行递归
                                        if (i > 100) {       //控制一下不至于栈溢出。
                                            stringBuilder2.append("...");
                                            break;
                                        }
                                        try {
                                            stringBuilder2.append(i + "# " + "<").append(filed.get(obj) != null ? printObj(obj, false) : "null").append("> ");
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }

                                    }


                                }
                                // 刪除空行。
                                if (stringBuilder.lastIndexOf("\n") == stringBuilder.length() - 1)
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                //为了给 list的 小标题 折行 使之对齐 层次更清晰。。
                                stringBuilder.append("\n");
                                if (depth > 1)
                                    for (int j = 0; j < depth; j++) {
                                        stringBuilder.append("    ");
                                    }

                                stringBuilder.append("[List]@" + filed.getName() + " {" + stringBuilder2 + "} ");
                            } else {
                                // 深度之内，如果为空或者大小为0。
                                if (stringBuilder.lastIndexOf("\n") == stringBuilder.length() - 1)
                                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                                stringBuilder.append("\n");
                                if (depth > 1)
                                    for (int j = 0; j < depth; j++) {
                                        stringBuilder.append("    ");
                                    }

                                // 是null 也要显示。
                                stringBuilder.append("[List]@" + filed.getName()).append(list == null ? ":null" : " size:0");
                            }

                        } else { // 在深度之外
                            stringBuilder.append("[List]@" + filed.getName()).append(list == null ? ":null" : " size:0");
                        }
                        depth--;
                    }


                }
        }
        return stringBuilder.toString();
    }


    private static boolean checkType(Class<?> aClass) {
        if (aClass.getName().equals("java.lang.String") || aClass.getName().equals("int") || aClass.getName().equals("long") || aClass.getName().equals("double")
                || aClass.getName().equals("boolean") || aClass.getName().equals("float") || aClass.getName().equals("[C"))
            return true;
        return false;
    }


}
