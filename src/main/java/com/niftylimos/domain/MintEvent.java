package com.niftylimos.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints =
        @UniqueConstraint(columnNames = {
                "_block_hash",
                "_block_index",
                "_tx_hash",
                "_tx_index",
                "_transfer_index",
                "_token_id",
        })
)
public class MintEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MINT_SEQ")
    private Long id;

    @Column(name = "_block_hash", nullable = false)
    private String blockHash;

    @Column(name = "_block_index", nullable = false)
    private Long block;

    @Column(name = "_tx_hash", nullable = false)
    private String txHash;

    @Column(name = "_tx_index", nullable = false)
    private Long txIndex;

    @Column(name = "_transfer_index", nullable = false)
    private Long transferIndex;

    @Column(name = "_token_id", nullable = false)
    private Long tokenId;
}
