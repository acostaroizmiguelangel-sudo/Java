
import java.util.ArrayList;
import java.util.Random;

public class quicksort {

    private static void swap(ArrayList<Integer> arr, int j, int k) {
        int temp = arr.get(j);
        arr.set(j, arr.get(k));
        arr.set(k, temp);
    }

    private static int partition(ArrayList<Integer> arr, int low, int high) {
        int pivot = arr.get(high);
        int j = low - 1;

        for (int k = low; k < high; k++) {
            if (arr.get(k) <= pivot) {
                j++;
                swap(arr, j, k);
            }
        }
        swap(arr, j + 1, high);
        return j + 1;
    }

    private static void quicksort(ArrayList<Integer> arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quicksort(arr, low, pi - 1);
            quicksort(arr, pi + 1, high);
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        ArrayList<Integer> numeros = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            numeros.add(random.nextInt(100) + 1);
        }

        System.out.println("El arreglo antes de ordenarlo:");
        System.out.println(numeros);

        quicksort(numeros, 0, numeros.size() - 1);

        System.out.println("\nEl arreglo después de ordenarlo:");
        System.out.println(numeros);
    }
}