package no.uib.pathwaymatcher.util;

import java.io.Serializable;
import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * @author Luis Sánchez <luis.sanchez@uib.no>
 */
public class MapList<S, T> implements Serializable {

    protected Map<S, List<T>> map = new HashMap<>();

    public boolean add(S identifier, T elem) {
        List<T> aux = getOrCreate(identifier);
        return aux.add(elem);
    }

    public boolean add(S identifier, Set<T> set) {
        List<T> aux = getOrCreate(identifier);
        return aux.addAll(set);
    }

    public boolean add(S identifier, List<T> list) {
        List<T> aux = getOrCreate(identifier);
        return aux.addAll(list);
    }

    public void addAll(MapList<S, T> map) {
        for (S s : map.keySet()) {
            this.add(s, map.getElements(s));
        }
    }

    public List<T> getElements(S identifier) {
        return map.get(identifier);
    }

    /**
     * Get the value list using a key, or create an empty value list for the key.
     *
     * @param identifier
     * @return
     */
    private List<T> getOrCreate(S identifier) {
        List<T> list = map.get(identifier);
        if (list == null) {
            list = new ArrayList<T>();
            map.put(identifier, list);
        }
        return list;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }


    public Set<S> keySet() {
        return map.keySet();
    }

    /**
     * Removes a key from the Map with its corresponding list.
     *
     * @param key
     * @return
     */
    public List<T> remove(S key) {
        return this.map.remove(key);
    }

    /**
     * Gathers all the elements of all the value lists of the map.
     *
     * @return
     */
    public List<T> values() {
        List<T> rtn = new ArrayList<>();
        for (List<T> ts : map.values()) {
            rtn.addAll(ts);
        }
        return rtn;
    }

    /**
     * Return the number of entry keys in the map.
     * @return
     */
    public int size() {
        return map.size();
    }

    public Set<Map.Entry<S, List<T>>> entrySetInternal() {
        return map.entrySet();
    }

    public boolean containsKey(S key) {
        return map.containsKey(key);
    }

    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return "MapList{" +
                "map=" + map +
                '}';
    }
}
