import java.util.ArrayList;
import java.util.Scanner;

public class ARREGLO3 {
    public static void main(String[] args) {
        ArrayList<Integer> arr = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            arr.add(i);
        }

        System.out.println("Valores que están en el arreglo:");
        for (int i = 0; i < arr.size(); i++) {
            System.out.println("Índice " + i + " : " + arr.get(i));
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("¿Qué número quieres ingresar? ");
        int n = sc.nextInt();
        System.out.print("¿En qué posición quieres que lo ponga? ");
        int posi = sc.nextInt();

        arr.add(posi, n);

        System.out.println("\nYa actualizado:");
        for (int i = 0; i < arr.size(); i++) {
            System.out.println("Índice " + i + " : " + arr.get(i));
        }
    }
}
