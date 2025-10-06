package interfaces;

import java.util.List;

public interface IServiceCrud<T> {
    void add(T t);
    List<T> getAll();
    void update(T t);
    void delete(int id);
    T getById(int id);


}
