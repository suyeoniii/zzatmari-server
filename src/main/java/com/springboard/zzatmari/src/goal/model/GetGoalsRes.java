package com.springboard.zzatmari.src.goal.model;

import com.springboard.zzatmari.src.list.model.GetListsRes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetGoalsRes {
    private List<GetListsRes> digitalDetox;
    private List<GetListsRes> selfDevelopment;
}