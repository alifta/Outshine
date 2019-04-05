package ca.mun.outshine;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    protected TextView profileName;
    private SharedPreferences sPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = v.findViewById(R.id.profile_name_text);

        sPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!sPreferences.getString(getString(R.string.key_nickname),"").isEmpty()) {
            profileName.setText(sPreferences.getString(getString(R.string.key_nickname),""));
        }

        insertNestedFragment();

        return v;
    }

    private void insertNestedFragment() {
        Fragment childFragment = new ProfileFragmentChild();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.profile_pref_fragment, childFragment).commit();
    }
}
