import java.util.Scanner;

public class Burbuja {
    public static void burbuja(int[] lista) {
        int n = lista.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (lista[j] > lista[j + 1]) {
                    int temp = lista[j];
                    lista[j] = lista[j + 1];
                    lista[j + 1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int[] datos = new int[5];

        System.out.println("Hola, ingrese 5 números y serán ordenados:");
        for (int i = 0; i < 5; i++) {
            System.out.print("Ingrese el número " + (i + 1) + ": ");
            datos[i] = sc.nextInt();
        }

        System.out.println("\nEstos son los datos antes de ordenar:");
        for (int num : datos) {
            System.out.print(num + " ");
        }

        burbuja(datos);

        System.out.println("\nEstos son los datos después de ordenar:");
        for (int num : datos) {
            System.out.print(num + " ");
        }

        sc.close();
    }
}
