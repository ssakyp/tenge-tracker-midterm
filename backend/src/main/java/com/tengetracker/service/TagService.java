package com.tengetracker.service;

import com.tengetracker.dto.patch.TagPatchRequest;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.ResourceNotFoundException;
import com.tengetracker.model.Tag;
import com.tengetracker.model.Transaction;
import com.tengetracker.repository.TagRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TransactionRepository transactionRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll(Sort.by("name"));
    }

    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
    }

    @Transactional
    public Tag createTag(Tag tag) {
        tag.setId(null);
        tag.setName(tag.getName().trim());
        if (tag.getColor() == null) tag.setColor("#64748b");
        ensureUniqueName(tag.getName(), null);
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag updateTag(Long id, Tag details) {
        Tag tag = getTagById(id);
        String name = details.getName().trim();
        ensureUniqueName(name, id);
        tag.setName(name);
        tag.setColor(details.getColor() == null ? "#64748b" : details.getColor());
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag patchTag(Long id, TagPatchRequest patch) {
        Tag tag = getTagById(id);
        if (patch.getName() != null) {
            String name = patch.getName().trim();
            ensureUniqueName(name, id);
            tag.setName(name);
        }
        if (patch.getColor() != null) tag.setColor(patch.getColor());
        return tagRepository.save(tag);
    }

    /** Deleting a tag only removes the label: the join rows are removed, the transactions stay. */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = getTagById(id);
        for (Transaction transaction : transactionRepository.findByTags_Id(id)) {
            transaction.getTags().remove(tag);
        }
        tagRepository.delete(tag);
    }

    private void ensureUniqueName(String name, Long currentId) {
        tagRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Tag '" + name + "' already exists (id: " + existing.getId() + ")");
                });
    }
}
