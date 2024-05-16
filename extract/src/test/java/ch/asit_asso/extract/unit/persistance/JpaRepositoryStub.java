package ch.asit_asso.extract.unit.persistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public abstract class JpaRepositoryStub<T> implements PagingAndSortingRepository<T, Integer> {
    protected List<T> content;

    public JpaRepositoryStub() {
        this.content = new ArrayList<>();
    }


    public JpaRepositoryStub(Collection<T> initialContent) {
        this.content.addAll(initialContent);
    }

    @Override
    public @NotNull Iterable<T> findAll(@NotNull Sort sort) {
        throw new NotImplementedException();
    }



    @Override
    public @NotNull Page<T> findAll(@NotNull Pageable pageable) {
        throw new NotImplementedException();
    }



    @Override
    public <S extends T> @NotNull S save(@NotNull S entity) {
        Optional<T> existingEntity = this.findById(this.getEntityId(entity));

        if (existingEntity.isEmpty())
        {
            this.setEntityId(entity, this.getNextId());

        } else {
            this.content.remove(existingEntity.get());
        }

        this.content.add(entity);

        return entity;
    }


    private int getNextId() {

        OptionalInt maxValue = this.content.stream().mapToInt(this::getEntityId).max();

        if (maxValue.isEmpty()) {
            return 1;
        }

        return maxValue.getAsInt() + 1;
    }



    @Override
    public <S extends T> @NotNull Iterable<S> saveAll(@NotNull Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();

        for (S entity : entities) {
            savedEntities.add(this.save(entity));
        }

        return savedEntities;
    }



    @Override
    public @NotNull Optional<T> findById(@NotNull Integer id) {

        return this.content.stream()
                           .filter(recoveryCode -> this.getEntityId(recoveryCode) == id)
                           .findFirst();
    }



    @Override
    public boolean existsById(@NotNull Integer id) {

        return !this.findById(id).isEmpty();
    }



    @Override
    public @NotNull Iterable<T> findAll() {

        return this.content.stream().toList();
    }



    @Override
    public @NotNull Iterable<T> findAllById(@NotNull Iterable<Integer> idsToFind) {
        List<T> foundCodes = new ArrayList<>();

        for (Integer id : idsToFind) {
            Optional<T> entity = this.findById(id);

            if (entity.isEmpty()) {
                continue;
            }

            foundCodes.add(entity.get());
        }

        return foundCodes;
    }



    @Override
    public long count() {
        return this.content.size();
    }



    @Override
    public void deleteById(@NotNull Integer idToRemove) {
        Optional<T> entityToRemove = this.findById(idToRemove);

        if (entityToRemove.isEmpty()) {
            return;
        }

        this.content.remove(entityToRemove.get());
    }



    @Override
    public void delete(@NotNull T entity) {
        this.content.remove(entity);
    }



    @Override
    public void deleteAllById(@NotNull Iterable<? extends Integer> entitiesIds) {
        entitiesIds.forEach(id -> this.deleteById(id));
    }



    @Override
    public void deleteAll(@NotNull Iterable<? extends T> entities) {
        entities.forEach(recoveryCode -> this.content.remove(recoveryCode));
    }



    @Override
    public void deleteAll() {
        this.content.clear();
    }



    public abstract @NotNull Integer getEntityId(@NotNull T entity);



    public abstract void setEntityId(@NotNull T entity, @NotNull Integer newId);

}
