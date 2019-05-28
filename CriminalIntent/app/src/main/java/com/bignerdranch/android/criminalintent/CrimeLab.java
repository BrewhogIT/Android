package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;

    private CrimeLab(Context context){
        mCrimes = new ArrayList<>();
    }

    public static CrimeLab get(Context context){
        if (sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public List <Crime> getCrimes(){
        return mCrimes;
    }

    public Crime getCrime(UUID id){

        for (Crime someCrime: mCrimes){
            if (id.equals(someCrime.getId())){
                return someCrime;
            }
        }

        return null;
    }

    public void addCrime (Crime c){
        mCrimes.add(c);
    }
    public void delCrime(UUID id){
        Iterator iterator = mCrimes.iterator();

        while (iterator.hasNext()){
            Crime someCrime = (Crime) iterator.next();
            if (id.equals(someCrime.getId())){
                iterator.remove();
            }
        }
    }
}
