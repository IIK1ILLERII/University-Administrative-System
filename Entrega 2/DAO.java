package dao;

import java.util.List;
import java.util.Optional;

public interface DAO<T> {

    Optional<T> get(int id);
    List<T> getAll();
    boolean save(T t);
    boolean update(T t);
    boolean delete(T t);
    boolean deleteById(int id);

    int count();
    boolean exists(int id);

    default void handleException(String operation, Exception e) {
        System.err.println("Error en " + operation + ": " + e.getMessage());
        e.printStackTrace();
    }
}