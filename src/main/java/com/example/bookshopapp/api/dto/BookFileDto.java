package com.example.bookshopapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookFileDto implements Comparable<BookFileDto> {
    private String fileType;
    private String fileTypeDescription;
    private String fileSize;
    private String hash;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BookFileDto)) {
            return false;
        }
        return fileType.equals(((BookFileDto) obj).fileType);
    }

    @Override
    public int compareTo(BookFileDto o) {
        return this.fileType.compareTo(o.fileType);
    }
}
