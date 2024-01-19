package com.example.repository;

import com.example.entity.Sight;
import com.example.parameter.SightQueryParameter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MockSightDAO {
    private final List<Sight> sightDB = new ArrayList<>();
    @PostConstruct
    private void initDB(){
        sightDB.add(new Sight("123","123","123","123","123","123"));
        sightDB.add(new Sight("456","456","456","456","456","456"));
    }

    public Sight insert(Sight sight){
        sightDB.add(sight);
        return sight;
    }

    public void delete(String name){
        sightDB.removeIf(p->p.getSightName().equals(name));
    }

    public Optional<Sight> find(String name) {
        return sightDB.stream().filter(p -> p.getSightName().equals(name)).findFirst();
    }
    //p->p.getSightName().toUpperCase().contains(name.toUpperCase())).collect(Collectors.toList());

    public List<Sight> getSightsByZone(SightQueryParameter param) {
        String keyword = Optional.ofNullable(param.getKeyword()).orElse("");
        String orderBy = param.getOrderBy();
        String sortRule = param.getSortRule();
        Comparator<Sight> comparator = genSortComparator(orderBy, sortRule);

        return sightDB.stream()
                .filter(p -> p.getZone().contains(keyword))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<Sight> genSortComparator(String orderBy, String sortRule) {
        Comparator<Sight> comparator = (p1, p2) -> 0;
        if (Objects.isNull(orderBy) || Objects.isNull(sortRule)) {
            return comparator;
        }

        if (orderBy.equalsIgnoreCase("category")) {
            comparator = Comparator.comparing(Sight::getCategory);
        }

        return sortRule.equalsIgnoreCase("desc")
                ? comparator.reversed()
                : comparator;
    }
}
